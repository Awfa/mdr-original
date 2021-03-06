package org.mamasdelrio.android;

import android.widget.DatePicker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mamasdelrio.android.logic.DatePickerHelper;
import org.mamasdelrio.android.logic.TimeStamper;
import org.mamasdelrio.android.testutil.AssertionHelper;
import org.mamasdelrio.android.util.BundleKeys;
import org.mamasdelrio.android.util.Constants;
import org.mamasdelrio.android.util.JsonKeys;
import org.mamasdelrio.android.util.JsonValues;
import org.mamasdelrio.android.widget.DniOrNameView;
import org.mamasdelrio.android.widget.LocationView;
import org.mamasdelrio.android.widget.SelectCommunityView;
import org.mamasdelrio.android.widget.SelectOneView;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DoPregnancyActivity}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DoPregnancyActivityTest {
  private DoPregnancyActivity activity;

  @Before
  public void before() {
    activity = Robolectric.setupActivity(DoPregnancyActivity.class);
  }

  @Test
  public void uiElementsVisible() {
    assertThat(activity.dniOrName)
        .isVisible()
        .isEnabled();
    assertThat(activity.community)
        .isVisible()
        .isEnabled();
    assertThat(activity.birthDate)
        .isVisible()
        .isEnabled();
    assertThat(activity.lastPeriodKnown)
        .isVisible()
        .isEnabled();
    assertThat(activity.takeControl)
        .isVisible()
        .isEnabled();
    assertThat(activity.controlMonth)
        .isVisible()
        .isEnabled();
    assertThat(activity.lastPeriodDateLabel)
        .isVisible()
        .isDisabled();
    // starts enabled because yes is the 0th position
    assertThat(activity.lastPeriodDate)
        .isVisible()
        .isEnabled();
    assertThat(activity.send)
        .isVisible()
        .isDisabled();
    assertThat(activity.location).isVisible();
  }

  @Test
  public void selectOnesRightSize() {
    String[] labels = activity.getResources().getStringArray(
        R.array.yes_no_dk_na_labels);
    assertThat(activity.lastPeriodKnown.getSpinner()).hasCount(labels.length);
    assertThat(activity.takeControl.getSpinner()).hasCount(labels.length);
    assertThat(activity.controlMonth).hasCount(
        Constants.NUM_MONTH_IN_PREGNANCY);
  }

  @Test
  public void isReadyToBeSentCorrect() {
    // We're going to swap this out
    DniOrNameView dniOrNameView = mock(DniOrNameView.class);
    when(dniOrNameView.isComplete()).thenReturn(false);
    activity.dniOrName = dniOrNameView;
    activity.onDniOrNameClicked(null);
    // Should start out disabled.
    assertNotReadyToBeSent();

    when(dniOrNameView.isComplete()).thenReturn(true);
    activity.dniOrName = dniOrNameView;
    activity.onDniOrNameClicked(null);
    assertReadyToBeSent();
  }

  @Test
  public void lastPeriodDateDisables() {
    // starts enabled because yes is selected first
    assertThat(activity.lastPeriodDate).isEnabled();
    activity.lastPeriodKnown.getSpinner().setSelection(1);
    assertThat(activity.lastPeriodDate).isDisabled();
    activity.lastPeriodKnown.getSpinner().setSelection(2);
    assertThat(activity.lastPeriodDate).isDisabled();
    activity.lastPeriodKnown.getSpinner().setSelection(3);
    assertThat(activity.lastPeriodDate).isDisabled();
    activity.lastPeriodKnown.getSpinner().setSelection(0);
    assertThat(activity.lastPeriodDate).isEnabled();
  }

  @Test
  public void getUserFriendlyMessageCorrect() {
    String actual = activity.getUserFriendlyMessage();
    assertThat(actual)
        .contains("Hola Mamás del Río. Tenemos una gestante nueva en")
        .doesNotContain("$");
  }

  @Test
  public void updateControlMonthVisibilityCorrect() {
    // should be visible only when "yes" is selected for do you take controls
    int yesIndex = 0;
    activity.takeControl.getSpinner().setSelection(yesIndex);
    assertControlMonthEnabled();

    // 4 total options
    activity.takeControl.getSpinner().setSelection(yesIndex + 1);
    assertControlMonthDisabled();
    activity.takeControl.getSpinner().setSelection(yesIndex + 2);
    assertControlMonthDisabled();
    activity.takeControl.getSpinner().setSelection(yesIndex + 3);
    assertControlMonthDisabled();

    activity.takeControl.getSpinner().setSelection(yesIndex);
    assertControlMonthEnabled();
  }

  @Test
  public void addValuesToMapCorrect() {
    String targetDateTime = "pregnancy time, behbeh";
    TimeStamper timeStamperMock = mock(TimeStamper.class);
    when(timeStamperMock.getFriendlyDateTime()).thenReturn(targetDateTime);
    DatePickerHelper dphMock = mock(DatePickerHelper.class);
    activity.setDatePickerHelper(dphMock);
    LocationView locationViewMock = mock(LocationView.class);
    activity.location = locationViewMock;

    // Dni or name
    DniOrNameView dniOrNameMock = mock(DniOrNameView.class);
    activity.dniOrName = dniOrNameMock;

    // Select Community
    SelectCommunityView communityMock = mock(SelectCommunityView.class);
    activity.community = communityMock;

    // birth date
    String targetBirthDate = "test birth date";
    when(dphMock.getFriendlyString(activity.birthDate)).thenReturn(
        targetBirthDate);
    // last period known
    SelectOneView lastPeriodMock = mock(SelectOneView.class);
    String lastPeriodKnownTarget = "last period known val";
    when(lastPeriodMock.getValueForSelected()).thenReturn(
        lastPeriodKnownTarget);
    activity.lastPeriodKnown = lastPeriodMock;
    // take control
    SelectOneView takeControlMock = mock(SelectOneView.class);
    String takeControlTarget = "take control value";
    when(takeControlMock.getValueForSelected()).thenReturn(takeControlTarget);
    activity.takeControl = takeControlMock;

    // control month
    int targetControlMonth = 5;
    int targetControlMonthIndex = targetControlMonth - 1; // 0 indexed
    activity.controlMonth.setSelection(targetControlMonthIndex);

    // last period date
    String targetPeriodDate = "period date";
    when(dphMock.getFriendlyString(activity.lastPeriodDate)).thenReturn(
        targetPeriodDate);

    Map<String, Object> map = new HashMap<>();
    activity.addValuesToMap(map, timeStamperMock);

    AssertionHelper.assertCommonKeysPresent(map, targetDateTime,
        JsonValues.Forms.PREGNANCIES);
    verify(dniOrNameMock, times(1)).addValuesToMap(map,
        JsonKeys.Pregnancies.HAS_DNI, JsonKeys.Pregnancies.DNI,
        JsonKeys.Pregnancies.NAMES);
    verify(communityMock, times(1)).addValuesToMap(map,
        JsonKeys.Pregnancies.COMMUNITY);
    verify(lastPeriodMock, times(1)).addValuesToMap(map,
        JsonKeys.Pregnancies.PERIOD_KNOWN);
    verify(takeControlMock, times(1)).addValuesToMap(map,
        JsonKeys.Pregnancies.TAKE_CONTROLS);

    assertThat(map).contains(
        entry(JsonKeys.Pregnancies.BIRTH_DATE, targetBirthDate),
        entry(JsonKeys.Pregnancies.PERIOD_DATE, targetPeriodDate),
        entry(JsonKeys.Pregnancies.CONTROL_MONTH, targetControlMonth));

    AssertionHelper.assertAddValuesCalledOnLocationView(locationViewMock, map);
  }

  @Test
  public void getControlMonthCorrect() {
    // should handle 0, last month, etc and account for zero indexing
    // starts at 0
    assertThat(activity.getControlMonth()).isEqualTo(1);

    int lastMonthIndex = 8;
    int lastMonth = lastMonthIndex + 1;
    activity.controlMonth.setSelection(lastMonthIndex);
    assertThat(activity.getControlMonth()).isEqualTo(lastMonth);
  }

  private void assertReadyToBeSent() {
    assertThat(activity.isReadyToBeSent()).isTrue();
    assertThat(activity.send).isEnabled();
  }

  private void assertNotReadyToBeSent() {
    assertThat(activity.isReadyToBeSent()).isFalse();
    assertThat(activity.send).isDisabled();
  }

  private void assertControlMonthDisabled() {
    assertThat(activity.controlMonthLabel).isDisabled();
    assertThat(activity.controlMonth).isDisabled();
  }

  private void assertControlMonthEnabled() {
    assertThat(activity.controlMonthLabel).isEnabled();
    assertThat(activity.controlMonth).isEnabled();
  }
}
