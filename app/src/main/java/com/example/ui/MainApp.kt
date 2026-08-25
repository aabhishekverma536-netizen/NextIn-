package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.auth.AuthScreen
import com.example.ui.customer.ClinicBookingScreen
import com.example.ui.customer.CustomerExploreScreen
import com.example.ui.customer.CustomerLiveTokenScreen
import com.example.ui.customer.SalonBookingScreen
import com.example.ui.owner.OwnerDashboardScreen
import com.example.ui.owner.OwnerProfileQrScreen
import com.example.ui.owner.OwnerServicesScreen
import com.example.ui.privacy.PrivacyPolicyScreen
import com.example.ui.splash.SplashScreen

@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val allBusinesses by viewModel.allBusinesses.collectAsStateWithLifecycle()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val selectedBusiness by viewModel.selectedBusiness.collectAsStateWithLifecycle()
    val currentBusinessServices by viewModel.currentBusinessServices.collectAsStateWithLifecycle()
    val activeLiveBooking by viewModel.activeLiveBooking.collectAsStateWithLifecycle()
    val customerActiveBooking by viewModel.customerActiveBooking.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        label = "screen_transition",
        modifier = modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            is Screen.Splash -> {
                SplashScreen(
                    onSplashFinished = {
                        viewModel.finishSplash()
                    }
                )
            }

            is Screen.Auth -> {
                AuthScreen(
                    onLoginSuccess = { session ->
                        viewModel.login(session)
                    },
                    onOpenPrivacyPolicy = {
                        viewModel.openPrivacyPolicy(Screen.Auth)
                    }
                )
            }

            is Screen.CustomerExplore -> {
                BackHandler {
                    viewModel.logout()
                }
                CustomerExploreScreen(
                    businesses = allBusinesses,
                    allBookings = allBookings,
                    activeCustomerBooking = customerActiveBooking,
                    onSelectBusiness = { biz ->
                        viewModel.selectBusiness(biz)
                    },
                    onViewLiveToken = { booking ->
                        viewModel.selectLiveBooking(booking)
                    },
                    onSwitchToOwner = {
                        viewModel.switchRole()
                    },
                    onLogout = {
                        viewModel.logout()
                    },
                    onOpenPrivacyPolicy = {
                        viewModel.openPrivacyPolicy(Screen.CustomerExplore)
                    }
                )
            }

            is Screen.CustomerSalonBooking -> {
                BackHandler {
                    viewModel.navigateTo(Screen.CustomerExplore)
                }
                selectedBusiness?.let { biz ->
                    SalonBookingScreen(
                        business = biz,
                        services = currentBusinessServices,
                        userSession = userSession!!,
                        onBack = { viewModel.navigateTo(Screen.CustomerExplore) },
                        onBookingConfirmed = { booking ->
                            viewModel.selectLiveBooking(booking)
                        },
                        onConfirmBooking = { b, n, p, e, s, notes ->
                            viewModel.bookSalon(b, n, p, e, s, notes)
                        }
                    )
                }
            }

            is Screen.CustomerClinicBooking -> {
                BackHandler {
                    viewModel.navigateTo(Screen.CustomerExplore)
                }
                selectedBusiness?.let { biz ->
                    ClinicBookingScreen(
                        business = biz,
                        existingBookings = allBookings,
                        userSession = userSession!!,
                        onBack = { viewModel.navigateTo(Screen.CustomerExplore) },
                        onBookingConfirmed = { booking ->
                            viewModel.selectLiveBooking(booking)
                        },
                        onConfirmBooking = { b, n, p, e, d, slot, notes ->
                            viewModel.bookClinic(b, n, p, e, d, slot, notes)
                        }
                    )
                }
            }

            is Screen.CustomerLiveToken -> {
                BackHandler {
                    viewModel.navigateTo(Screen.CustomerExplore)
                }
                activeLiveBooking?.let { booking ->
                    val biz = allBusinesses.find { it.id == booking.businessId }
                    val bizBookings = allBookings.filter { it.businessId == booking.businessId }

                    CustomerLiveTokenScreen(
                        booking = booking,
                        business = biz,
                        allBookingsForBusiness = bizBookings,
                        onBack = { viewModel.navigateTo(Screen.CustomerExplore) },
                        onCancelBooking = { bookingId ->
                            viewModel.cancelBooking(bookingId)
                            viewModel.navigateTo(Screen.CustomerExplore)
                        }
                    )
                } ?: run {
                    viewModel.navigateTo(Screen.CustomerExplore)
                }
            }

            is Screen.OwnerDashboard -> {
                BackHandler {
                    viewModel.logout()
                }
                selectedBusiness?.let { biz ->
                    val bizBookings = allBookings.filter { it.businessId == biz.id }

                    OwnerDashboardScreen(
                        business = biz,
                        bookings = bizBookings,
                        onCallNext = { viewModel.callNextToken(biz.id) },
                        onCompleteBooking = { id -> viewModel.completeBooking(id) },
                        onMarkNoShow = { id -> viewModel.markNoShow(id) },
                        onCancelBooking = { id -> viewModel.cancelBooking(id) },
                        onAddWalkIn = { name, srv, prc, dur ->
                            viewModel.addWalkIn(biz.id, name, srv, prc, dur)
                        },
                        onBroadcastDelay = { minutes, msg ->
                            viewModel.broadcastDelayAlert(biz.id, minutes, msg)
                        },
                        onClearDelay = {
                            viewModel.clearDelayAlert(biz.id)
                        },
                        onSwitchToCustomer = { viewModel.switchRole() },
                        onManageServices = { viewModel.navigateTo(Screen.OwnerServices) },
                        onViewProfileQr = { viewModel.navigateTo(Screen.OwnerProfileQr) },
                        onLogout = { viewModel.logout() }
                    )
                }
            }

            is Screen.OwnerServices -> {
                BackHandler {
                    viewModel.navigateTo(Screen.OwnerDashboard)
                }
                selectedBusiness?.let { biz ->
                    OwnerServicesScreen(
                        business = biz,
                        services = currentBusinessServices,
                        onSaveService = { srv -> viewModel.saveService(srv) },
                        onDeleteService = { srv -> viewModel.deleteService(srv) },
                        onUpdateBusinessConfig = { updatedBiz -> viewModel.updateBusinessProfile(updatedBiz) },
                        onBack = { viewModel.navigateTo(Screen.OwnerDashboard) }
                    )
                }
            }

            is Screen.OwnerProfileQr -> {
                BackHandler {
                    viewModel.navigateTo(Screen.OwnerDashboard)
                }
                selectedBusiness?.let { biz ->
                    OwnerProfileQrScreen(
                        business = biz,
                        onUpdateProfile = { updatedBiz -> viewModel.updateBusinessProfile(updatedBiz) },
                        onBack = { viewModel.navigateTo(Screen.OwnerDashboard) },
                        onOpenPrivacyPolicy = {
                            viewModel.openPrivacyPolicy(Screen.OwnerProfileQr)
                        }
                    )
                }
            }

            is Screen.PrivacyPolicy -> {
                BackHandler {
                    viewModel.closePrivacyPolicy()
                }
                PrivacyPolicyScreen(
                    onNavigateBack = {
                        viewModel.closePrivacyPolicy()
                    }
                )
            }
        }
    }
}
