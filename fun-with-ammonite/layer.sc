#!/usr/bin/env amm

import ammonite.ops._

// This basically implements a little manual state machine that reflects Layer's historical-export function.
//
// Call this as "./layer.sc <function>", where <function> is one of the @main methods. Note that the .sc suffix
// isn't actually necessary, but helps IntelliJ realize that this is a Scala file. (IntelliJ support for these
// scripts is pretty weak, but it at least helps with the syntax, formatting, and local vals.)
//
// Start with the start() function, which kicks off an export. Then call check() to see if the currently-running
// export is done. Once it is, call download() to fetch it. At any time after, you can run decode() to decrypt it.

// You need to have the API token and App ID in environment variables in order to run the various Layer
// API calls
lazy val layerApiToken = sys.env("LAYER_API_TOKEN")
lazy val layerAppId = sys.env("LAYER_APP_ID")

// These are the local files that we use to keep track of what's currently going on
lazy val wd = pwd
// The URL that Layer gives us for checking on how the export process is going
lazy val statusUrlPath = wd / "currentStatusUrl.txt"
// The URL that Layer gives us to download the most recent export
lazy val downloadUrlPath = wd / "currentDownloadUrl.txt"
// The index of this download, so we can maintain previous ones
lazy val downloadCounterPath = wd / "downloadCounter"
// The location of the public key we are giving Layer to encrypt the export
lazy val publicKeyPath = wd / "layer-export-key.pub"
// The path to the current download's AES key:
def aesKey(counter: Int) = wd / s"aesKey-$counter"
// The path to the current download's init vector:
def aesIV(counter: Int) = wd / s"aesIV-$counter"
// The downloaded file itself:
def downloadedFilePath(version: Int) = wd / s"download_$version.tar.gz.enc"

// These are the headers that Layer wants for pretty much every request:
def standardHeaders = Map(
  "Accept" -> "application/vnd.layer+json; version=3.0",
  "Authorization" -> s"Bearer $layerApiToken",
  "Content-Type" -> "application/json"
)

// Given the text of the response from Layer, pretty-print it
def printResponse(response: String) = {
  val json = ujson.read(response)
  println(s"Response from Layer:\n${ujson.transform(json, new ujson.StringRenderer(indent = 2))}")
}

// At any given time, we have one most-recent download that we are currently working with; it is basically
// a version number. It gets updated in start(), and can be fetched any time with this.
def getDownloadCounter(): Int = {
  val downloadCounterStr = read.lines(downloadCounterPath).headOption.getOrElse(throw new Exception(s"Couldn't read downloadCounter!"))
  downloadCounterStr.toInt
}

implicit class RichObj(obj: scala.collection.mutable.LinkedHashMap[String, ujson.Value]) {
  // Convenience function for getting a required string-valued field from a JSON object
  def getString(name: String): String = obj.get(name).map(_.str).getOrElse(throw new Exception(s"Couldn't find a $name!"))
}

// Hello, world
// Calling this is a quick shortcut to demonstrate that the script is compiling and executing successfully.
@main
def hello() = {
  println(s"The API token is $layerApiToken")
  println(s"The App ID is $layerAppId")
}

// Kicks off a Layer Historical Export.
// You can only run this once a day!!!
@main
def start() = {
  val response = requests.post(
    s"https://api.layer.com/apps/$layerAppId/exports",
    headers = standardHeaders
  ).text

  printResponse(response)

  val json = ujson.read(response).obj
  json.get("status_url").map(_.str) match {
    case Some(statusUrl) => {
      // This is the URL that we will ping in check():
      println(s"\nStatus URL: $statusUrl")
      write.over(statusUrlPath, statusUrl)

      // Bump the download counter:
      val downloadCounter: Int = getDownloadCounter()
      write.over(downloadCounterPath, (downloadCounter + 1).toString)
    }

    case None => {
      println(s"\nDidn't get a statusUrl; something went wrong here!")
    }
  }
}

// Checks the current status of the most recent export attempt.
@main
def check() = {
  // Get the URL for checking on the current export, which we set in start():
  val statusUrl = read! statusUrlPath
  val response = requests.get(
    statusUrl,
    headers = standardHeaders
  ).text

  printResponse(response)
  val obj = ujson.read(response).obj
  val status = obj.getString("status")

  status match {
    case "pending" => println(s"Still waiting for this to complete...")
    case "completed" => {
      // Excellent -- grab the URL we will need for download():
      val downloadUrl = obj.getString("download_url")
      write.over(downloadUrlPath, downloadUrl)

      // Store the key and initialization vector that we will need in decode() for this archive:
      val downloadCounter = getDownloadCounter()
      val keyPath = aesKey(downloadCounter)
      val ivPath = aesIV(downloadCounter)
      val key = obj.getString("encrypted_aes_key")
      val iv = obj.getString("aes_iv")
      write.over(keyPath, key)
      write.over(ivPath, iv)

      println(s"We're ready to roll! Call the download function next.")
    }
    case other => println(s"Got unexpected status $other")
  }
}

// Download the most recent export.
@main
def download() = {
  val downloadCounter: Int = getDownloadCounter()

  val downloadUrl = read! downloadUrlPath
  val filename = downloadedFilePath(downloadCounter)
  println(s"Downloading $downloadUrl to $filename")

  // We're just going to call out to curl to do the actual download, since that is easier than connecting all the
  // streams ourselves. Note that the current working directory is required as an implicit param for %, which
  // is the magic syntax to invoke an external process.
  implicit val cwd = wd
  %("curl", downloadUrl, "-o", filename)
  println("Done!")
}

// Runs a small bash script, and returns the resulting output as a sequence of lines.
def runScript(script: String): Seq[String] = {
  implicit val cwd = wd
  %%("bash", "-c", script).out.lines
}

// Once a given run is fully downloaded, you can use this to decode it.
// Note that this takes the version as a parameter, so that it can be used for arbitrary older versions
// of the data. If not specified, the most recent download will be used.
@main
def decode(version: Int = getDownloadCounter()) = {
  // The one big limitation of Ammonite Scripts seems to be the lack of ability to pipe one process to another.
  // That's definitely unfortunate, and requires us to work through these bits of script instead.
  // The actual script here is basically as provided by Layer, decomposed into a few steps to make it more
  // comprehensible.

  // The files are encrypted with a key that has been encrypted with our public key. So first, we need to
  // decrypt that using our private key:
  val encryptedKey = read! aesKey(version)
  val key =
    runScript(
      s"""echo "$encryptedKey" | base64 --decode | openssl rsautl -decrypt -inkey layer-export-key.pem | hexdump -ve '1/1 "%.2x"'"""
    ).head
  println(key)

  // Then fetch the initialization vector, and massage it into the right format:
  val encodedIV = read! aesIV(version)
  val iv = runScript(s"""echo $encodedIV | base64 --decode | hexdump -ve '1/1 "%.2x"'""").head
  println(iv)

  // Actually decrypt the archive, which gives us a gzipped tarball:
  val targzPath = wd / s"download_$version.tar.gz"
  runScript(s"""openssl enc -in ${downloadedFilePath(version)} -out $targzPath -d -aes-256-cbc -K "$key" -iv "$iv" """)
  println(s"Decrypted to $targzPath")

  // Unzip and untar it:
  runScript(s"""gunzip $targzPath""")
  val tarPath = wd / s"download_$version.tar"
  runScript(s"""tar -xvf $tarPath""")

  // Finally, put it in its own little directory, and clean up:
  val outputDir = wd / s"decoded_$version"
  mkdir! outputDir
  mv(wd / "export.json", outputDir / "export.json")
  rm! tarPath

  println(s"Done! Decoded output is in ${outputDir / "export.json"}")
}

// Upload the public key to Layer
@main
def uploadPublic() = {
  val publicKey = read! publicKeyPath
  val jsonBody = ujson.Obj(
    "public_key" -> publicKey
  )

  val response = requests.put(
    s"https://api.layer.com/apps/$layerAppId/export_security",
    headers = standardHeaders,
    data = jsonBody.toString
  ).text

  printResponse(response)
}

// Fetch the current public key
@main
def getPublic() = {
  val response = requests.get(
    s"https://api.layer.com/apps/$layerAppId/export_security",
    headers = standardHeaders,
  ).text

  printResponse(response)
}
