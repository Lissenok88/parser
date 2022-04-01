package com.lyudmila.topliba;

import java.util.ArrayList;
import java.util.HashMap;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ToplibaBot extends TelegramLongPollingBot {
    private static final String START = "/start";
    private static final String BUTTON_NAME = "Найти книгу.";
    private static final String TEXT_START1 = "Это телеграмбот для поиска и скачивания книг с " +
            "сайта [Topliba](https://topliba.com/)";
    private static final String TEXT_START2 = "Чтобы найти книгу нажмите книпку \"Найти книгу\"";
    private static final String TEXT_BUTTON = "Напишите без ошибок название книги или имя автора.";
    private static final String TEXT_SEARCH = "Ищем книги по запросу: ";
    private static final String TEXT_ERROR_SEARCH = "Искомая книга или автор не найден.";
    private static final String BOT_USERNAME = "ToplibaBot";
    private static final String BOT_TOKEN = "2114228543:AAF0Zcly2maELCGzcVkuw4ttcjk3MGIL_nU";
    private final HashMap<String, ArrayList<BookInformation>> listRequest = new HashMap<>();


    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() != null && update.getMessage().hasText()) {
            parseMassage(update.getMessage(), listRequest);
        } else if (update.hasCallbackQuery()) {
            String str = update.getCallbackQuery().getData();
            parseButton(str, update.getCallbackQuery(), listRequest);
        }
    }

    private void parseMassage(Message getMessage, HashMap<String, ArrayList<BookInformation>> listRequest) {
        MessageProcessing messageProcessing = new MessageProcessing();
        switch (getMessage.getText()) {
            case START -> messageProcessing.message(getMessage, TEXT_START1 +
                    System.lineSeparator().repeat(2) + TEXT_START2);
            case BUTTON_NAME -> messageProcessing.message(getMessage, TEXT_BUTTON);
            default -> {
                messageProcessing.message(getMessage, TEXT_SEARCH + getMessage.getText());
                ArrayList<BookInformation> foundBooks = new ArrayList<>(ToplibaParser.parser(getMessage.getText()));
                listRequest.put(getMessage.getText(), foundBooks);
                if (!foundBooks.isEmpty()) {
                    messageProcessing.messageListOfBooks("1", getMessage, foundBooks);
                } else {
                    messageProcessing.message(getMessage, TEXT_ERROR_SEARCH);
                }
            }
        }
    }

    private void parseButton(String selectButton, CallbackQuery callbackQuery, HashMap<String,
            ArrayList<BookInformation>> searchBook) {
        MessageProcessing messageProcessing = new MessageProcessing();
        if (selectButton.contains("fb2")) {
            try {
                messageProcessing.deleteMessage(callbackQuery.getMessage());
                Console.output(callbackQuery.getData());
                SendDocument sendDocument = new SendDocument(callbackQuery.getMessage().getChatId().toString(),
                        new InputFile(callbackQuery.getData()));
                sendDocument.setCaption("File download");
                execute(sendDocument);
            } catch (TelegramApiException e) {
                messageProcessing.message(callbackQuery.getMessage(), "error download");
                e.printStackTrace();
            }
        } else if (selectButton.contains("->") || (selectButton.contains("<-"))) {
            messageProcessing.editMessageListOfBooks(callbackQuery.getData(), callbackQuery.getMessage(), searchBook);
        } else {
            Console.output("button number:" + callbackQuery.getData());
            messageProcessing.messageAboutBook(callbackQuery.getMessage().getChatId().toString(),
                    ToplibaParser.fillElements(callbackQuery.getData()));
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
