import SwiftUI
import shared
import AuthenticationServices

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, supportedInterfaceOrientationsFor window: UIWindow?) -> UIInterfaceOrientationMask {
        return MainViewControllerKt.isLandscapeRequested ? .landscape : .portrait
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        LandscapeEffectKt.onRequestLandscape = {
            DispatchQueue.main.async {
                guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene else { return }
                let prefs = UIWindowScene.GeometryPreferences.iOS(interfaceOrientations: .landscape)
                scene.requestGeometryUpdate(prefs) { _ in }
                scene.keyWindow?.rootViewController?.setNeedsUpdateOfSupportedInterfaceOrientations()
            }
        }
        LandscapeEffectKt.onRequestPortrait = {
            DispatchQueue.main.async {
                guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene else { return }
                let prefs = UIWindowScene.GeometryPreferences.iOS(interfaceOrientations: .portrait)
                scene.requestGeometryUpdate(prefs) { _ in }
                scene.keyWindow?.rootViewController?.setNeedsUpdateOfSupportedInterfaceOrientations()
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
