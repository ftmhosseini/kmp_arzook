import SwiftUI
import shared
import GoogleSignIn

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let vc = MainViewControllerKt.MainViewController()
        GoogleSignInButtonKt.onGoogleSignInRequested = {
            GoogleSignInHandler.shared.signIn(
                webClientId: BuildConfig.shared.GOOGLE_CLIENT_ID_WEB,
                from: vc
            )
        }
        return vc
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

class GoogleSignInHandler {
    static let shared = GoogleSignInHandler()

    func signIn(webClientId: String, from viewController: UIViewController) {
        let config = GIDConfiguration(clientID: BuildConfig.shared.GOOGLE_CLIENT_ID_IOS,
                                      serverClientID: webClientId)
        GIDSignIn.sharedInstance.configuration = config
        GIDSignIn.sharedInstance.signIn(withPresenting: viewController) { result, error in
            guard let user = result?.user,
                  let idToken = user.idToken?.tokenString else {
                print("[GoogleSignIn] failed: \(error?.localizedDescription ?? "no token")")
                return
            }
            print("[GoogleSignIn] got id_token, length=\(idToken.count)")
            self.callSocialLogin(idToken: idToken)
        }
    }

    private func callSocialLogin(idToken: String) {
        guard let url = URL(string: "https://api.arzook.ca/api/auth/social-login") else { return }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("https://arzook.ca", forHTTPHeaderField: "Origin")
        request.setValue("https://arzook.ca/", forHTTPHeaderField: "Referer")
        request.httpBody = try? JSONSerialization.data(withJSONObject: ["idToken": idToken])

        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error { print("[SocialLogin] network error: \(error)"); return }
            let statusCode = (response as? HTTPURLResponse)?.statusCode ?? 0
            let raw = String(data: data ?? Data(), encoding: .utf8) ?? "nil"
            print("[SocialLogin] status=\(statusCode) data=\(raw)")
            guard let data = data,
                  let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                  let accessToken = json["accessToken"] as? String,
                  let tokenType = json["tokenType"] as? String else {
                print("[SocialLogin] failed: \(raw)")
                return
            }
            let fullToken = "\(tokenType) \(accessToken)"
            DispatchQueue.main.async {
                MainViewControllerKt.sharedAuthViewModel.receiveToken(token: fullToken)
            }
        }.resume()
    }
}
