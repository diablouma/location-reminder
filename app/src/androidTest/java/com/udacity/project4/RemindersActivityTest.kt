package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val mockedDatasource = mock(ReminderDataSource::class.java)

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        initKoinDependencies()
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun shouldSaveANewLocationReminder() = runBlocking {
        repository.saveReminder(
            ReminderDTO(
                "TestingTitle",
                "TestingDescription",
                "TestingLocation",
                1.12,
                1.23
            )
        )

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("A Testing Title"))
        onView(withId(R.id.reminderDescription)).perform(
            typeText("A Testing Description"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText("A Testing Title")).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun shouldShowSnackbarWhenThereWasAnErrorLoadingTheReminders() = runBlocking {
        initKoinDependencies(true)

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText("Reminders not found")))

        activityScenario.close()
    }

    @Test
    fun shouldShowToastMessageWhenReminderWasSavedSuccessfully() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("A Testing Title"))
        onView(withId(R.id.reminderDescription)).perform(
            typeText("A Testing Description"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.saveReminder)).perform(click())

        val activity = getActivity(activityScenario)
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(activity?.window?.decorView))))
            .check(
                matches(
                    isDisplayed()
                )
            )

        activityScenario.close()
    }

    @Test
    fun shouldShowSnackbarWithValidationErrorWhenTitleIsNotCorrect() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText(""))
        onView(withId(R.id.reminderDescription)).perform(
            typeText("A Testing Description"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        activityScenario.close()
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    private fun initKoinDependencies(withMockedDataSource: Boolean = false) {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()

        if (withMockedDataSource) {
            runBlocking {
                `when`(mockedDatasource.getReminders()).thenReturn(Result.Error("Reminders not found"))
            }
        }

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            if (withMockedDataSource) {
                single { mockedDatasource as ReminderDataSource }
            } else {
                single { RemindersLocalRepository(get()) as ReminderDataSource }
            }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }
}
