package com.lyudmila.topliba;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ToplibaBot extends TelegramLongPollingBot {
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    ArrayList<BookInformation> data = new ArrayList<>();
    int length = 0;

    public void message(Message message, String update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        try {
            setButton(sendMessage);
            sendMessage.setText(update);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            Console.output(e.getMessage(), true);
        }
    }

    public void messageListOfBooks(String pageStr, Message message) {
        int page = pageCalculation(pageStr);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        if (data != null) {
            String result = readPage(page);
            sendMessage.setText(result);
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(readButton(page));
            inlineKeyboardMarkup.setKeyboard(rowList);
            try {
                sendMessage.setText(result);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                execute(sendMessage);
            } catch (Exception e) {
                Console.output(e.getMessage(), true);
            }
        }
    }

    private int pageCalculation(String pageButton) {
        int page = 0;
        int index = pageButton.indexOf(">");
        if (index != -1) {
            String str = pageButton.substring(index + 1);
            if (!str.equals(""))
                page = Integer.parseInt(str);
        } else {
            index = pageButton.indexOf("<");
            if (index != -1) {
                String str = pageButton.substring(0, index);
                if (!str.equals(""))
                    page = Integer.parseInt(str);
            } else {
                if (!pageButton.equals(""))
                    page = Integer.parseInt(pageButton);
            }
        }
        return page;
    }

    public void messageAboutBook(String message, BookInformation bookInformation) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();

        String result = "*" + bookInformation.getTitle() + "*\n\n\r\r" + bookInformation.getDescription() + "\n\n\r\r"
                + "_" + bookInformation.getFragment() + "_";
        if (!bookInformation.getUrlFb2().equals("")) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("fb2");
            button.setCallbackData(bookInformation.getUrlFb2());
            keyboardButtonsRow.add(button);
            sendMessage.setText(result);
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(keyboardButtonsRow);
            inlineKeyboardMarkup.setKeyboard(rowList);
            try {
                sendMessage.setText(result);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                execute(sendMessage);
            } catch (Exception e) {
                Console.output(e.getMessage(), true);
            }
        } else {
            sendMessage.setText(result);
            try {
                setButton(sendMessage);
                sendMessage.setText(result);
                execute(sendMessage);
            } catch (Exception e) {
                Console.output(e.getMessage(), true);
            }
        }
    }

    public String readPage(int page) {
        StringBuilder result = new StringBuilder("[Topliba](https://topliba.com/)");
        result.append(System.lineSeparator().repeat(2));
        result.append("*Страница " + page + " из " + length + "*");
        result.append(System.lineSeparator().repeat(2));
        int index = page * 5 - 5;
        for (int i = 0; i < 5; i++) {
            if (index < data.size() && data.get(index).getUrl() != null) {
                result.append("*" + data.get(index).getPosition() + ".* " + data.get(index).getTitle());
                result.append("[  >>](" + data.get(index).getUrl() + ")");
                result.append(System.lineSeparator().repeat(2));
                index++;
                if (index == data.size())
                    break;
            }
        }
        return result.toString();
    }

    public List<InlineKeyboardButton> readButton(int page) {
        int index = page * 5 - 5;
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        if (page != 1) {
            int position = page - 1;
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(position + "<-");
            button.setCallbackData(position + "<-");
            keyboardButtonsRow.add(button);
            Console.output("button: <- " + position, true);
        }
        for (int i = 0; i < 5; i++) {
            if (index < data.size() && data.get(index).getUrl() != null) {
                int iterator = index + 1;
                InlineKeyboardButton button2 = new InlineKeyboardButton(Integer.toString(iterator));
                button2.setCallbackData(data.get(index).getUrl());
                keyboardButtonsRow.add(button2);
                Console.output("button: " + data.get(index).getPosition() + "url: " + data.get(index).getUrl(), true);
                index++;
                if (index == data.size())
                    break;
            }
        }
        if (page != length) {
            int nextPage = page + 1;
            InlineKeyboardButton button = new InlineKeyboardButton("->" + Integer.toString(nextPage));
            button.setCallbackData("->" + Integer.toString(nextPage));
            keyboardButtonsRow.add(button);
            Console.output("button: -> " + nextPage, true);
        }
        return keyboardButtonsRow;
    }

    public void dataLength() {
        length = data.size() / 5;
        Console.output(Integer.toString(length), true);
        if (length % 5 != 0)
            length = length + 1;
        Console.output(Integer.toString(length), true);
    }

    public void deleteMessage(Message message) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(message.getChatId()));
        deleteMessage.setMessageId(message.getMessageId());
        deleteMessage.getChatId();
        deleteMessage.getMessageId();
        try {
            execute(deleteMessage);
        } catch (Exception ex) {
            Console.output("Error in deleteMessage", true);
            Console.output(ex.getMessage(), true);
        }
    }

    public void setButton(SendMessage sendMessage) {
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("Запуск бота."));
        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() != null && update.getMessage().hasText()) {
            parseMassage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            String str = update.getCallbackQuery().getData();
            parseButton(str, update.getCallbackQuery());
        }
    }

    private void parseMassage(Message getMessage) {
        switch (getMessage.getText()) {
            case "/start":
                message(getMessage,"Для старта, нажмите кнопку \"Запуск бота\"");
                break;
            case "Запуск бота.":
                message(getMessage,
                        "Привет, я бот по поиску книг на сайте.\n\n Введите название книги или автора.\n\n\r\r");
                break;
            default:
                try {
                    message(getMessage, "Поиск...");
                    Console.output(getMessage.getText(), true);
                    data.clear();
                    data = new ArrayList<>(ToplibaParser.parser(getMessage.getText()));
                    length = 0;
                    dataLength();
                    if (length > 0) {
                        messageListOfBooks("1", getMessage);
                    } else {
                        message(getMessage, "Искомая книга или автор не найден.");
                    }
                } catch (Exception e) {
                    message(getMessage, "Искомая книга или автор не найден.");
                    e.printStackTrace();
                    Console.output(e.getMessage(), true);
                }
                break;
        }
    }

    private void parseButton(String selectButton, CallbackQuery callbackQuery) {
        if (selectButton.contains("fb2")) {
            Console.output("Button press fb2", true);
            try {
                deleteMessage(callbackQuery.getMessage());
                callbackQuery.getData();
                message(callbackQuery.getMessage(), "Файл скачен.");
            } catch (Exception e) {
                Console.output("error message fb2...", true);
                e.printStackTrace();
            }
        } else if (selectButton.contains("->") || (selectButton.contains("<-"))) {
            try {
                Console.output("button press >", true);
                deleteMessage(callbackQuery.getMessage());
                messageListOfBooks(callbackQuery.getData(), callbackQuery.getMessage());
            } catch (Exception e) {
                Console.output("error message press > ...", true);
                e.printStackTrace();
            }
        } else {
            try {
                deleteMessage(callbackQuery.getMessage());
                Console.output("button press number" + callbackQuery.getMessage().toString(), true);
                messageAboutBook(callbackQuery.getMessage().getChatId().toString(),
                        ToplibaParser.fillElements(callbackQuery.getData()));
                Console.output("button good", true);
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
