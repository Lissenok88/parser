package com.lyudmila.topliba;

import java.util.ArrayList;
import java.util.HashMap;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ToplibaBot extends TelegramLongPollingBot {
    private HashMap<String, ArrayList<BookInformation>> listRequest = new HashMap<>();

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
        Messages messages = new Messages();
        switch (getMessage.getText()) {
            case "/start":
                messages.message(getMessage, "Для старта, нажмите кнопку \"Запуск бота\"");
                break;
            case "Запуск бота.":
                messages.message(getMessage,
                        "Привет, я бот по поиску книг на сайте.\n\n Введите название книги или автора.\n\n\r\r");
                break;
            default:
                try {
                    messages.message(getMessage, "Поиск...");
                    Console.output(getMessage.getText(), true);
                    ArrayList<BookInformation> foundBooks = new ArrayList<>(ToplibaParser.parser(getMessage.getText()));
                    listRequest.put(getMessage.getText(), foundBooks);
                    if (!foundBooks.isEmpty()) {
                        messages.messageListOfBooks("1", getMessage, listRequest);
                    } else {
                        messages.message(getMessage, "Искомая книга или автор не найден.");
                    }
                } catch (Exception e) {
                    messages.message(getMessage, "Искомая книга или автор не найден.");
                    e.printStackTrace();
                    Console.output(e.getMessage(), true);
                }
                break;
        }
    }

    private void parseButton(String selectButton, CallbackQuery callbackQuery, HashMap<String, ArrayList<BookInformation>> searchBook) {
        Messages messages = new Messages();
        if (selectButton.contains("fb2")) {
            try {
                messages.deleteMessage(callbackQuery.getMessage());
                SendDocument sendDocument = new SendDocument(callbackQuery.getMessage().getChatId().toString(), new InputFile(callbackQuery.getData()));
                execute(sendDocument);
                messages.message(callbackQuery.getMessage(), "Файл скачен.");
            } catch (Exception e) {
                Console.output("error message fb2...", true);
                e.printStackTrace();
            }
        } else if (selectButton.contains("->") || (selectButton.contains("<-"))) {
            try {
                messages.editMessageListOfBooks(callbackQuery.getData(), callbackQuery.getMessage(), searchBook);
            } catch (Exception e) {
                Console.output("error message press > ...", true);
                e.printStackTrace();
            }
        } else {
            try {
                messages.deleteMessage(callbackQuery.getMessage());
                messages.messageAboutBook(callbackQuery.getMessage().getChatId().toString(),
                        ToplibaParser.fillElements(callbackQuery.getData()));
            } catch (Exception e) {
                Console.output("error message...", true);
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "ToplibaBot";
    }

    @Override
    public String getBotToken() {
        return "2114228543:AAF0Zcly2maELCGzcVkuw4ttcjk3MGIL_nU";
    }
}
