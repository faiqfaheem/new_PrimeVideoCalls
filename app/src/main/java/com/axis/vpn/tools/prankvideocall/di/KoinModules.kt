package com.axis.vpn.tools.prankvideocall.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.axis.vpn.tools.prankvideocall.data.local.AppDatabase
import com.axis.vpn.tools.prankvideocall.data.local.CustomCallerDao
import com.axis.vpn.tools.prankvideocall.data.remote.ApiService
import com.axis.vpn.tools.prankvideocall.data.repository.CallerRepository
import com.axis.vpn.tools.prankvideocall.data.repository.CallerRepositoryImpl
import com.axis.vpn.tools.prankvideocall.data.repository.HomeRepository
import com.axis.vpn.tools.prankvideocall.data.viewModels.CallerViewModel
import com.axis.vpn.tools.prankvideocall.data.viewModels.HomeDataViewModel
import com.axis.vpn.tools.prankvideocall.data.viewModels.StartViewModel
import com.axis.vpn.tools.prankvideocall.ui.fragments.language.LanguageViewModel
import com.axis.vpn.tools.prankvideocall.utils.constants.SharedPreferenceUtils
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class KoinModules {

    /* -------------------------------------- Managers -------------------------------------- */
//    private val managerModules = module {
//        single {
//            InternetManagerBlue(
//                androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            )
//        }
//    }

    /* -------------------------------------- Utils -------------------------------------- */
//    private val utilsModules = module {
//        single { InternetManager(androidContext()) }
//        single {
//            SharedPreferenceUtils(
//                androidContext().getSharedPreferences("app_preferences", Application.MODE_PRIVATE)
//            )
//        }
//    }

    /* -------------------------------------- Firebase -------------------------------------- */
//    private val firebaseModule = module {
//        // RemoteConfigurationTik singleton (your wrapper)
//        single { RemoteConfiguration(get(), get()) }
//
//        // FirebaseRemoteConfig singleton (required by AppOpenAds)
//        single { Firebase.remoteConfig }  // <-- THIS is missing
//    }

    /* -------------------------------------- App Open Ads -------------------------------------- */
//    private val appOpenAdModule = module {
//        single { AppOpenAdManager(get(), get(), get()) }
//        single { AppOpenAdsConfig(get(), get(), get()) }
//    }

    /* -------------------------------------- Banner Ads -------------------------------------- */
//    private val bannerAdModule = module {
//        single { DataSourceLocalBanner() }
//        single { DataSourceRemoteBanner(context = get()) }
//        single { RepositoryBannerImpl(get(), get()) }
//        single { UseCaseBanner(get(), get(), get(), get()) }
//        viewModel { ViewModelBanner(get()) }
//    }

    /* -------------------------------------- Interstitial Ads -------------------------------------- */
//    private val interAdModule = module {
//        single { InterstitialAdsConfig(get(), get(), get(), get()) }
//        // Inject RemoteConfigurationTik for dynamic ad IDs
//    }

    /* -------------------------------------- Native Ads -------------------------------------- */
//    private val nativeAdModule = module {
//        single { DataSourceLocalNative() }
//        single { DataSourceRemoteNative(context = get()) }
//        single { RepositoryNativeImpl(get(), get()) }
//        single {
//            UseCaseNative(get(), get(), get(), get())
//        }
////        viewModel { ViewModelNative(get()) }
//        viewModel { ViewModelNativeSplash(get()) }
//        viewModel { ViewModelNativeLanguage(get()) }
////        viewModel { ViewModelNativeHome(get()) }
////        viewModel { ViewModelNativeExit(get()) }
//    }

    val networkModule = module {

        single {

            Retrofit.Builder()
                .baseUrl("https://viberaytech.com/")
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()

        }

        single<ApiService> {

            get<Retrofit>()
                .create(ApiService::class.java)

        }

    }
    private val utilsModule = module {
        single {
            SharedPreferenceUtils(
                androidContext().getSharedPreferences(
                    "app_preferences",
                    Context.MODE_PRIVATE
                )
            )
        }

    }
    private val callerRepositoryModule = module {


        single<CallerRepository> {

            CallerRepositoryImpl(
                get()
            )

        }


    }

    private val callerViewModelModule = module {


        viewModel {

            StartViewModel(
                repository = get()
            )

        }



        viewModel {

            CallerViewModel(
                repository = get()
            )

        }


    }

    /* -------------------------------------- Language Module -------------------------------------- */
    private val languageModule = module {
        viewModel { LanguageViewModel() }
    }

    @OptIn(KoinExperimentalAPI::class)
    private val domainModules = lazyModule {
        single { GeneralObserver() }
    }
    /* -------------------------------------- All Modules List -------------------------------------- */
//    private val nativeAdModule = module {
//        single { DataSourceLocalNative() }
//        single { DataSourceRemoteNative(context = get()) }
//        single { RepositoryNativeImpl(get(), get()) }
//        single { UseCaseNative(get(), get(), get(), get()) }
//        viewModel { ViewModelNative(get()) }
//        viewModel { ViewModelNativeLarge(get()) }
//    }

    //    val modulesList = listOf(
////        utilsModules,
////        managerModules,
////        firebaseModule,
////        appOpenAdModule,
////        interAdModule,
////        nativeAdModule,
//        languageModule
//    )
    val databaseModule = module {


        single {

            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                "caller_database"
            )
                .build()

        }



        single<CustomCallerDao> {


            get<AppDatabase>()
                .customCallerDao()


        }

    }
    val repositoryModule = module {

        single {
            HomeRepository(
                get()
            )
        }

    }


    val homeViewModelModule = module {

        viewModel {

            HomeDataViewModel(
                get()
            )

        }

    }
    val modulesList = listOf(

        utilsModule,

        networkModule,

        databaseModule,

        callerRepositoryModule,

        callerViewModelModule,

        repositoryModule,

        homeViewModelModule,

        languageModule

    )
    val backgroundModuleList = listOf(domainModules)
}