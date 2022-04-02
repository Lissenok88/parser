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
    private static final String COMMAND_START = "/start";
    private static final String SEARCH_BUTTON = "Найти книгу.";
    private static final String START_MESSAGE1 = "Это телеграмбот для поиска и скачивания книг с сайта.";
    private static final String START_MESSAGE2 = "[Topliba](https://topliba.com/)";
    private static final String START_MESSAGE3 = "Чтобы найти книгу нажмите книпку \"Найти книгу\"";
    private static final String MESSAGE_SEARCH_BUTTON = "Напишите без ошибок название книги или имя автора.";
    private static final String SEARCH_MESSAGE = "Ищем книги по запросу: ";
    private static final String SEARCH_ERROR = "Искомая книга или автор не найден.";
    private static final String DOWNLOAD_FILE_MESSAGE = "Открыть файл";
    private static final String DOWNLOAD_FILE_ERROR = "Что-то пошло не так. Невозможно скачать файл.";
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
            case COMMAND_START -> messageProcessing.message(getMessage, START_MESSAGE1 +
                    System.lineSeparator().repeat(2) + START_MESSAGE2 +
                    System.lineSeparator().repeat(2) + START_MESSAGE3);
            case SEARCH_BUTTON -> messageProcessing.message(getMessage, MESSAGE_SEARCH_BUTTON);
            default -> {
                messageProcessing.message(getMessage, SEARCH_MESSAGE + getMessage.getText());
                ArrayList<BookInformation> foundBooks = new ArrayList<>(ToplibaParser.parser(getMessage.getText()));
                listRequest.put(getMessage.getText(), foundBooks);
                if (!foundBooks.isEmpty()) {
                    messageProcessing.messageListOfBooks(getMessage, foundBooks);
                } else {
                    messageProcessing.message(getMessage, SEARCH_ERROR);
                }
            }
        }
    }

    private void parseButton(String selectButton, CallbackQuery callbackQuery, HashMap<String,
            ArrayList<BookInformation>> listRequest) {
        MessageProcessing messageProcessing = new MessageProcessing();
        if (selectButton.contains("fb2")) {
            try {
                messageProcessing.deleteMessage(callbackQuery.getMessage());
                Console.output(callbackQuery.getData());
                SendDocument sendDocument = new SendDocument(callbackQuery.getMessage().getChatId().toString(),
                        new InputFile(callbackQuery.getData()));
                sendDocument.setCaption(DOWNLOAD_FILE_MESSAGE);
                execute(sendDocument);
            } catch (TelegramApiException e) {
                messageProcessing.message(callbackQuery.getMessage(), DOWNLOAD_FILE_ERROR);
                e.printStackTrace();
            }
        } else if (selectButton.contains("->") || (selectButton.contains("<-"))) {
            messageProcessing.editMessageListOfBooks(callbackQuery.getData(), callbackQuery.getMessage(), listRequest);
        } else {
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
