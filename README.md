# Guppy Android
Guppy is a visual logger for both devs and testers alike that allows you to see and manage details 
about what network calls have been made in your current application session.

Version 1.0.0

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Guppy Architecture 
### OkHTTP HttpLoggingInterceptor
Square's OkHTTP already has a way for developers to log network requests as they happen. 
- Guppy is an implementation of [OkHTTP Logging Interceptor](https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor)
- `GuppyInterceptor.kt` and `GuppyActivity.kt` are separate with the idea in mind that we can support and build more Interceptors in the future.
- OkHTTP is the only supported network library in this first version.

### Logging Strategy
- Adding a `GuppyInterceptor.kt` to your OkHTTPClient instance will enable Guppy logging to a SQLite database. 
- The network logs can be viewed in the Shake UI, which uses the database.
- You can enable the Shake UI in your app by implementing `ShakeAction.kt` in your own BaseActivity, or extending `GuppyActivity.kt`.

## Implementing Guppy

### BaseActivity
- Since a Shake Gesture requires hooks into the Activity lifecycle, in your app's BaseActivity (or, any 
activity that you are making network requests you wish to inspect), you may extend this `GuppyActivity.kt` class.

```kotlin 
class BaseActivity : GuppyActivity() {
//...
}
```

- Alternatively, if you would prefer to implement this yourself, Guppy exposes a `ShakeAction.kt` interface which you can implement in your own Activity.

```kotlin
interface ShakeAction {

    fun setSensors()

    fun onShakeReceived(dialogFragment: GuppyDialogFragment?)

    fun buildGuppyDialogFragment(): GuppyDialogFragment?

    fun register()

    fun unregister()

    fun getGuppyData(): List<GuppyData>
}
```

### OkHTTP
In your instance of OkHTTP, Add the `GuppyInterceptor` class as an Interceptor.

```kotlin
    @Singleton
    @Provides
    fun providesOkClient(application: Application) : OkHttpClient {
        val okHttpBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        //...
        okHttpBuilder.addNetworkInterceptor(GuppyInterceptor())
        return okHttpBuilder.build()
    }
```

## Using Guppy

Unfortunately there is no shortcut for "Shake Gesture". In the Android Emulator, 
* Click on the overflow menu (three dots)
* Click Virtual Sensors
* Select the "Move" radio button
* Rapidly move the `X` or `Y` sliders back and forth.

You could also just use any physical device and shake it :)

![Requests](https://github.com/johnsonandjohnson/Guppy-Android/raw/master/screenshots/request_list.png)

![Request Detail](https://github.com/johnsonandjohnson/Guppy-Android/raw/master/screenshots/request_detail.png)

## Requirements

* Android Studio 3.5+
* minSDK = 21
* targetSDK = 28

## Installation

For now, before we host this in jcenter, import the contained `guppy` module into your project via Android Studio 3.5 or greater.

## Maintainers of this Android port
* Chris Corrado, ccorrad@its.jnj.com
* Yakun Li, yli276@its.jnj.com
