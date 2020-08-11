package sample

import platform.Foundation.*
import platform.Foundation.NSURLCredentialPersistence.*
import platform.darwin.NSObject

actual fun auth(url: String, username: String, password: String) {
    val nsUrl = NSURL.URLWithString(url) ?: throw IllegalArgumentException("could not convert url to NSURL")
    val handler = AuthenticationHandler(nsUrl, username, password)
    handler.authenticate()
}

class AuthenticationHandler(
    private val url: NSURL,
    private val username: String,
    private val password: String
) : NSObject(), NSURLSessionDelegateProtocol {

    private val session = NSURLSession.sessionWithConfiguration(NSURLSessionConfiguration.ephemeralSessionConfiguration, this, null)

    private fun log(msg: String) = println(msg)

    fun authenticate() {
        log("starting authentication")

        @Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
        val request = NSMutableURLRequest(this.url, 4 /* reloadIgnoringLocalAndRemoteCacheData */, 60000.0)

        request.HTTPMethod = "GET"
        val task = session.dataTaskWithRequest(request) { _, response, error ->
            log("response: $response")
            log("error: $error")
        }
        task.resume()
    }

    override fun URLSession(session: NSURLSession, didReceiveChallenge: NSURLAuthenticationChallenge, completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit) {
        // super.URLSession(session, didReceiveChallenge, completionHandler)
        log("got auth challenge")
        val credentials = NSURLCredential.create(username, password, NSURLCredentialPersistenceForSession)
        didReceiveChallenge.sender?.useCredential(credentials, didReceiveChallenge)
        completionHandler(NSURLSessionAuthChallengeUseCredential, credentials)
    }
}