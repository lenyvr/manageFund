package com.fund.manageFunds.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fund.manageFunds.domain.model.Fund;
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

    /*Test under construction
    @Test
    void subscribeToAFund(){
        //Arrange
        Subscription subscriptionToBeRetorned = getSubscription();
        Fund fundToBeRetorned = getFund();
        String testText = "test";

        when(fundServiceMock.getFund(fundToBeRetorned.getName())).thenReturn(fundToBeRetorned);

        //Act
        //Assert
    }*/

    private Subscription getSubscription(){
        Subscription  subscription = new Subscription();
        subscription.setClientEmail(clientEmailTest);
        subscription.setId("123");
        subscription.setInvestAmount(0D);
        subscription.setInitialAmount(100D);
        subscription.setState(SubscriptionState.CANCELED);
        return subscription;
    }

    private Fund getFund(){
        Fund fund = new Fund();
        fund.setMinAmount(1D);
        fund.setName("test");
        return  fund;
    }
}
