package com.fund.manageFunds.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fund.manageFunds.domain.exceptions.IsAlreadySubscribed;
import com.fund.manageFunds.domain.exceptions.NoAvailableAmount;
import com.fund.manageFunds.domain.exceptions.NoFundFound;
import com.fund.manageFunds.domain.model.Fund;
import com.fund.manageFunds.domain.model.NotificationOptions;
import com.fund.manageFunds.domain.model.Subscription;
import com.fund.manageFunds.domain.model.SubscriptionState;
import com.fund.manageFunds.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepositoryMock;
    @Mock
    private FundService fundServiceMock;
    @Mock
    private SmsService smsServiceMock;
    @Mock
    private EmailService emailServiceMock;

    @InjectMocks
    private SubscriptionService subscriptionService;

    final String  clientEmailTest = "test@test.com";
    private final Logger logger = (Logger) LoggerFactory.getLogger(SubscriptionService.class);

    /**
     * Method is called only with required parameter, in this case,
     * the filter goes for the method getByClient
     */
    @Test
    void getByClientFilteredShouldSearchByEmailOnly(){
        //Arrange
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        List<Subscription> subscriptionListToReturn = new ArrayList<>();
        when(subscriptionRepositoryMock.getByClient(anyString())).thenReturn(subscriptionListToReturn);
        String logExpected = "There aren't any transactions for this client";
        boolean logExecuted;

        //Act
        subscriptionService.getByClientFiltered(clientEmailTest, null, null);

        //Assert
        verify(subscriptionRepositoryMock, times(1)).getByClient(anyString());
        logExecuted = listAppender.list.stream()
                .anyMatch(event -> event.getMessage().contains(logExpected)
                        && event.getLevel().equals(Level.WARN));

        assertTrue(logExecuted, "Log warm was executed");


        logger.detachAppender(listAppender);
    }


    /**
     * Method is called only with required parameter and fund name.
     * When fund name and/or state is sent, the method called changes because the filters.
     * the filter goes for the method getSubscriptionsByClientFiltered.
     */
    @Test
    void getByClientFilteredShouldSearchByFilters(){
        //Arrange
        List<Subscription> subscriptionListToReturn = new ArrayList<>();
        subscriptionListToReturn.add(new Subscription());
        String fundNameTest = "test";
        when(subscriptionRepositoryMock.getSubscriptionsByClientFiltered(clientEmailTest, fundNameTest, null))
                .thenReturn(subscriptionListToReturn);

        //Act
        subscriptionService.getByClientFiltered(clientEmailTest, fundNameTest, null);

        //Assert
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(clientEmailTest, fundNameTest, null);

    }

    /**
     * For subscribe to a fund, the existence of that fund is verified, its verified that the client isn't subscribed
     * yet and also verifies the client amount respect the minimum asked for the fund and the owned by the client. at
     * the end, a notification its send according to client choice in the field setNotificationOption
     * In this case, all the verifications goes well
     */
    @Test
    void subscribeToAFundShouldWork1(){
        //Arrange
        Subscription subscriptionToBeRetorned = getSubscription();
        Fund fundToBeRetorned = getFund();
        List<Subscription> subscriptionList = List.of(subscriptionToBeRetorned);
        String notificationMessage = String.format("You have been successfully subscribed to the fund %s",
                subscriptionToBeRetorned.getFundName());
        String htmlBody = "<h1>Subscription confirmation</h1><p><b>" + notificationMessage + "</b></p>";
        String subject = "Subscription confirmation";

        when(fundServiceMock.getFund(fundToBeRetorned.getName())).thenReturn(fundToBeRetorned);
        when(subscriptionRepositoryMock.
                getSubscriptionsByClientFiltered(subscriptionToBeRetorned.getClientEmail(), null, SubscriptionState.OPENED.name()))
                .thenReturn(subscriptionList);
        when(subscriptionRepositoryMock
                .getSubscriptionsByClientFiltered(subscriptionToBeRetorned.getClientEmail(), subscriptionToBeRetorned.getFundName(), null))
                .thenReturn(subscriptionList);
        when(subscriptionRepositoryMock.save(subscriptionToBeRetorned)).thenReturn(subscriptionToBeRetorned);
        doNothing().when(emailServiceMock).sendEmail(subscriptionToBeRetorned.getClientEmail(),
                subject, htmlBody, notificationMessage);

        //Act
        subscriptionService.subscribeToAFund(subscriptionToBeRetorned);

        //Assert
        verify(emailServiceMock, times(1))
                .sendEmail(subscriptionToBeRetorned.getClientEmail(), subject, htmlBody, notificationMessage);
        verify(fundServiceMock, times(1)).getFund(fundToBeRetorned.getName());
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(subscriptionToBeRetorned.getClientEmail(), null,
                        SubscriptionState.OPENED.name());
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(subscriptionToBeRetorned.getClientEmail(),
                        subscriptionToBeRetorned.getFundName(), null);
        verify(subscriptionRepositoryMock, times(1)).save(subscriptionToBeRetorned);
    }

    /**
     * For subscribe to a fund, the existence of that fund is verified, its verified that the client isn't subscribed
     * yet and also verifies the client amount respect the minimum asked for the fund and the owned by the client. at
     * the end, a notification its send according to client choice in the field setNotificationOption
     * In this case, an exception is thrown because the fund sent doesn't exist
     */
    @Test
    void subscribeToAFundShouldThrowNoFundFoundException() {
        //Arrange
        String textTest = "test";
        Subscription subscriptionTest = getSubscription();
        subscriptionTest.setFundName(textTest);
        Exception exceptionObtained;
        NoFundFound noFundFound = new NoFundFound(subscriptionTest.getClientEmail(), subscriptionTest.getFundName());

        //Act
        exceptionObtained = assertThrowsExactly(NoFundFound.class,
                () -> subscriptionService.subscribeToAFund(subscriptionTest));

        //Assert
        verify(fundServiceMock, times(1)).getFund(subscriptionTest.getFundName());
        assertEquals(noFundFound.getMessage(), exceptionObtained.getMessage(),
                "Exception is thrown because the fund doesn't exist");

    }

    /**
     * For subscribe to a fund, the existence of that fund is verified, its verified that the client isn't subscribed
     * yet and also verifies the client amount respect the minimum asked for the fund and the owned by the client. at
     * the end, a notification its send according to client choice in the field setNotificationOption
     * In this case, an exception is thrown the client have an open subscription to the same fund
     */
    @Test
    void subscribeToAFundShouldThrowIsAlreadySubscribedException() {
        //Arrange
        Exception exceptionObtained;
        Fund fundToBeRetorned = getFund();
        Subscription subscriptionTest = getSubscription();
        Subscription subscription1 = getSubscription();
        Subscription subscription2 = getSubscription();
        subscription1.setDate("12/10/2025 05:10:10");
        subscription2.setState(SubscriptionState.OPENED);
        subscription2.setDate("13/10/2025 10:10:10");
        List<Subscription> subscriptionsByClientAndFund = List.of(subscription1, subscription2);
        IsAlreadySubscribed isAlreadySubscribed = new IsAlreadySubscribed(subscriptionTest.getClientEmail(), subscriptionTest.getFundName());

        when(fundServiceMock.getFund(fundToBeRetorned.getName())).thenReturn(fundToBeRetorned);
        when(subscriptionRepositoryMock.getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),
                subscriptionTest.getFundName(), null)).thenReturn(subscriptionsByClientAndFund);

        //Act
        exceptionObtained = assertThrowsExactly(IsAlreadySubscribed.class,
                () -> subscriptionService.subscribeToAFund(subscriptionTest));

        //Assert
        verify(fundServiceMock, times(1)).getFund(subscriptionTest.getFundName());
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),
                        subscriptionTest.getFundName(), null);
        assertEquals(isAlreadySubscribed.getMessage(), exceptionObtained.getMessage(),
                "Exception is thrown because client is already subscribed");

    }

    /**
     * For subscribe to a fund, the existence of that fund is verified, its verified that the client isn't subscribed
     * yet and also verifies the client amount respect the minimum asked for the fund and the owned by the client. at
     * the end, a notification its send according to client choice in the field setNotificationOption
     * In this case, an exception is thrown because the invest amount is less than the minimum amount asked for the fund
     */
    @Test
    void subscribeToAFundShouldThrowNoAvailableAmountException1() {
        //Arrange
        Exception exceptionObtained;
        Fund fundToBeRetorned = getFund();
        Subscription subscriptionTest = getSubscription();
        subscriptionTest.setInvestAmount(0.5D);
        List<Subscription> subscriptionsByClientAndFund = new ArrayList<>();
        NoAvailableAmount noAvailableAmount = new NoAvailableAmount("The amount invested is less than the minimum amount required in the fund");

        when(fundServiceMock.getFund(fundToBeRetorned.getName())).thenReturn(fundToBeRetorned);
        when(subscriptionRepositoryMock.getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),
                subscriptionTest.getFundName(), null)).thenReturn(subscriptionsByClientAndFund);
        when(subscriptionRepositoryMock.getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),null,
                SubscriptionState.OPENED.name())).thenReturn(subscriptionsByClientAndFund);

        //Act
        exceptionObtained = assertThrowsExactly(NoAvailableAmount.class,
                () -> subscriptionService.subscribeToAFund(subscriptionTest));

        //Assert
        verify(fundServiceMock, times(1)).getFund(subscriptionTest.getFundName());
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),
                        subscriptionTest.getFundName(), null);
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),null,
                        SubscriptionState.OPENED.name());
        assertEquals(noAvailableAmount.getMessage(), exceptionObtained.getMessage(),
                "Exception is thrown because minimum amount is not valid");
    }

    /**
     * For subscribe to a fund, the existence of that fund is verified, its verified that the client isn't subscribed
     * yet and also verifies the client amount respect the minimum asked for the fund and the owned by the client. at
     * the end, a notification its send according to client choice in the field setNotificationOption
     * In this case, an exception is thrown because the invest amount is less than the available amount
     */
    @Test
    void subscribeToAFundShouldThrowNoAvailableAmountException2() {
        //Arrange
        Exception exceptionObtained;
        Fund fundToBeRetorned = getFund();
        Subscription subscriptionTest = getSubscription();
        subscriptionTest.setInvestAmount(600000D);
        List<Subscription> subscriptionsByClientAndFund = new ArrayList<>();
        NoAvailableAmount noAvailableAmount = new NoAvailableAmount(subscriptionTest.getClientEmail(), fundToBeRetorned.getName());

        when(fundServiceMock.getFund(fundToBeRetorned.getName())).thenReturn(fundToBeRetorned);
        when(subscriptionRepositoryMock.getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),
                subscriptionTest.getFundName(), null)).thenReturn(subscriptionsByClientAndFund);
        when(subscriptionRepositoryMock.getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),null,
                SubscriptionState.OPENED.name())).thenReturn(subscriptionsByClientAndFund);

        //Act
        exceptionObtained = assertThrowsExactly(NoAvailableAmount.class,
                () -> subscriptionService.subscribeToAFund(subscriptionTest));

        //Assert
        verify(fundServiceMock, times(1)).getFund(subscriptionTest.getFundName());
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),
                        subscriptionTest.getFundName(), null);
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),null,
                        SubscriptionState.OPENED.name());
        assertEquals(noAvailableAmount.getMessage(), exceptionObtained.getMessage(),
                "Exception is thrown because invest amount is higher than available amount");
    }

    @Test
    void cancelFundSubscriptionShouldTrowIsAlreadySubscribedException() {
        //Arrange
        Exception exceptionObtained;
        Subscription subscriptionTest = getSubscription();
        List<Subscription> subscriptionsByClientAndFund = new ArrayList<>();
        String messageExpected =  String
                .format("There isn't any opened subscription from %s into fund %s to cancel.",
                        subscriptionTest.getClientEmail(), subscriptionTest.getFundName());
        when(subscriptionRepositoryMock.getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),
                subscriptionTest.getFundName(), null)).thenReturn(subscriptionsByClientAndFund);

        //Act
        exceptionObtained = assertThrowsExactly(IsAlreadySubscribed.class,
                () -> subscriptionService.cancelFundSubscription(subscriptionTest));

        //Assert
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),
                        subscriptionTest.getFundName(), null);
        assertEquals(messageExpected, exceptionObtained.getMessage(),
                "Exception is thrown because there isn't open subscriptions to close");

    }

    @Test
    void cancelFundSubscriptionShouldWork() {
        //Arrange
        Subscription subscriptionTest = getSubscription();
        Subscription subscription1 = getSubscription();
        subscription1.setState(SubscriptionState.OPENED);
        List<Subscription> subscriptionsByClientAndFund = List.of(subscription1);
        when(subscriptionRepositoryMock.getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),
                subscriptionTest.getFundName(), null)).thenReturn(subscriptionsByClientAndFund);
        when(subscriptionRepositoryMock.getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),null,
                SubscriptionState.OPENED.name())).thenReturn(subscriptionsByClientAndFund);
        when(subscriptionRepositoryMock.save(subscriptionTest)).thenReturn(subscriptionTest);

        //Act
        subscriptionService.cancelFundSubscription(subscriptionTest);

        //Assert
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),
                        subscriptionTest.getFundName(), null);
        verify(subscriptionRepositoryMock, times(1))
                .getSubscriptionsByClientFiltered(subscriptionTest.getClientEmail(),null,
                        SubscriptionState.OPENED.name());
        verify(subscriptionRepositoryMock, times(1))
                .save(subscriptionTest);
    }

    private Subscription getSubscription(){
        Subscription  subscription = new Subscription();
        subscription.setClientEmail(clientEmailTest);
        subscription.setId("123");
        subscription.setInvestAmount(10D);
        subscription.setInitialAmount(100D);
        subscription.setFundName("test");
        subscription.setState(SubscriptionState.CANCELED);
        subscription.setNotificationOption(NotificationOptions.EMAIL);
        return subscription;
    }

    private Fund getFund(){
        Fund fund = new Fund();
        fund.setMinAmount(1D);
        fund.setName("test");
        return  fund;
    }
}
