package io.karn.instagram

import io.karn.instagram.endpoints.*
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Ordered execution.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
        AccountTest::class,
        DirectMessagesTest::class,
        MediaTest::class,
        SearchTest::class,
        StoriesTest::class,
        LogoutTest::class
)
class SignedInTestSuite
