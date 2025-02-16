package com.marco_cavalli.lost_and_found;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AnonymousTest {

    private IdlingResource mActivityResource;

    @Rule
    public ActivityTestRule<AnonymousAuthActivity> mActivityTestRule =
            new ActivityTestRule<>(AnonymousAuthActivity.class);

    @Before
    public void setUp() {
        if (mActivityResource != null) {
            Espresso.unregisterIdlingResources(mActivityResource);
        }

        // Register Activity as idling resource
        mActivityResource = new BaseActivityIdlingResource(mActivityTestRule.getActivity());
        Espresso.registerIdlingResources(mActivityResource);
    }

    @After
    public void tearDown() {
        if (mActivityResource != null) {
            Espresso.unregisterIdlingResources(mActivityResource);
        }
    }

    @Test
    public void anonymousSignInTest() {
        // Sign out if possible
        signOutIfPossible();

        // Click sign in
        onView(allOf(withId(R.id.button_anonymous_sign_in),
                withText(R.string.sign_in), isDisplayed())).perform(click());

        // Make sure userID and email look right
        String idString = mActivityTestRule.getActivity().getString(R.string.id_fmt, "");
        String emailString = mActivityTestRule.getActivity().getString(R.string.email_fmt, "");

        onView(withText(startsWith(idString)))
                .check(matches(isDisplayed()));

        onView(withText(startsWith(emailString)))
                .check(matches(isDisplayed()));
    }

    private void signOutIfPossible() {
        try {
            onView(allOf(withId(R.id.button_anonymous_sign_out), isDisplayed()))
                    .perform(click());
        } catch (NoMatchingViewException e) {
            // Ignore
        }

    }
}
