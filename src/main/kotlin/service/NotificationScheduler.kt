package service

import bot
import dao.QuoteDao
import java.time.LocalTime
import java.time.temporal.ChronoUnit

object NotificationScheduler {

    init {

    }

    fun checkNotificationTime() {
        while(true) {
            val currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
            val quotes = QuoteDao.findAllWhereNotificationTime(currentTime.toString())
            for (quote in quotes) {

            }
        }
    }


//    public void checkNotificationTime() {
//        while (true) {
//            LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
//            List<Quote> quotes = quoteRepository.getAllWithGivenNotificationTime(currentTime);
//            if (!quotes.isEmpty()) {
//                for (Quote quote : quotes) {
//                    mentorBot.sendResponse(Response.builder()
//                        .responseMessage(quote.getText())
//                        .chatId(quote.getChatId())
//                        .build());
//                }
//            }
//            takeTimeOut(currentTime);
//        }
//    }
//
//    private void takeTimeOut(LocalTime timeBeforeNotification) {
//        long currentTimeAfterNotification = LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toSecondOfDay();
//        long sleepTimeAfterNotification = (timeBeforeNotification.toSecondOfDay() + 60 - currentTimeAfterNotification) * 1000;
//        try {
//            Thread.sleep(sleepTimeAfterNotification);
//        } catch (InterruptedException e) {
//            log.error("takeTimeOut: Error during sleep after notification", e);
//            Thread.currentThread().interrupt();
//        }
//    }


}