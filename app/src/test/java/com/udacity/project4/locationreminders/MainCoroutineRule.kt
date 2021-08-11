package com.udacity.project4.locationreminders

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class MainCoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()):
    TestWatcher(), // this make this rule a Junit rule
    TestCoroutineScope by TestCoroutineScope(dispatcher) {
    // above we are implementing TestCoroutineScope, we are passing the TestCoroutineDispatcher to it
    // it allows the MainCoroutineRUle the ability to control the timing of the coroutines by using
    // the given dispatcher

    override fun starting(description: Description?) { // equivalent to the @Before
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) { // equivalent to the @After
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}