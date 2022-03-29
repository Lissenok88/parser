package com.lyudmila.topliba;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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
        int page = 0;
        int index = pageStr.indexOf(">");
        if (index != -1) {
            String str = pageStr.substring(index + 1);
            if (!str.equals(""))
                page = Integer.parseInt(str);
        } else {
            index = pageStr.indexOf("<");
            if (index != -1) {
                String str = pageStr.substring(0, index);
                if (!str.equals(""))
                    page = Integer.parseInt(str);
            } else {
                if (!pageStr.equals(""))
                    page = Integer.parseInt(pageStr);
            }
        }
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
        String result = "[Topliba](https://topliba.com/)\n\n\r\r";
        result += "*Страница " + page + " из " + length + "*\n\n\r\r";
        int index = page * 5 - 5;
        for (int i = 0; i < 5; i++) {
            if (index < data.size() && data.get(index).getUrl() != null) {
                result += "*" + data.get(index).getPosition() + ".* " + data.get(index).getTitle() + "[  >>](" +
                        data.get(index).getUrl() + ")" + "\n\n\r\r";
                index++;
                if (index == data.size())
                    break;
            }
        }
        return result;
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
                int iterat = index + 1;
                InlineKeyboardButton button2 = new InlineKeyboardButton(Integer.toString(iterat));
                button2.setCallbackData(data.get(index).getUrl());
                keyboardButtonsRow.add(button2);
                Console.output("button: " + data.get(index).getPosition() + "url: " + data.get(index).getUrl(), true);
                index++;
                if (index == data.size())
                    break;
            }
        }
        if (page != length) {
            int rrr = page + 1;
            InlineKeyboardButton button = new InlineKeyboardButton("->" + Integer.toString(rrr));
            button.setCallbackData("->" + Integer.toString(rrr));
            keyboardButtonsRow.add(button);
            Console.output("button: -> " + rrr, true);
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
            switch (update.getMessage().getText()) {
                case "/start":
                    message(update.getMessage(),
                            "Для старта, нажмите кнопку \"Запуск бота\"");
                    break;
                case "Запуск бота.":
                    message(update.getMessage(),
                            "Привет, я бот по поиску книг на сайте.\n\n Введите название книги или автора.\n\n\r\r");
                    break;
                default:
                    try {
                        message(update.getMessage(), "Поиск...");
                        Console.output(update.getMessage().getText(), true);
                        data.clear();
                        data = new ArrayList<>(ToplibaParser.parser(update.getMessage().getText()));
                        length = 0;
                        dataLength();
                        if (length > 0) {
                            messageListOfBooks("1", update.getMessage());
                        } else {
                            message(update.getMessage(), "Искомая книга или автор не найден.");
                        }
                    } catch (Exception e) {
                        message(update.getMessage(), "Искомая книга или автор не найден.");
                        e.printStackTrace();
                        Console.output(e.getMessage(), true);
                    }
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            String str = update.getCallbackQuery().getData();
            if (str.contains("fb2")) {
                Console.output("Button press fb2", true);
                try {
                    deleteMessage(update.getCallbackQuery().getMessage());
                    update.getCallbackQuery().getData();
                    message(update.getCallbackQuery().getMessage(), "Файл скачен.");
                } catch (Exception e) {
                    Console.output("error message fb2...", true);
                    e.printStackTrace();
                }
            } else if (str.contains("->")) {
                try {
                    Console.output("button press >", true);
                    deleteMessage(update.getCallbackQuery().getMessage());
                    messageListOfBooks(update.getCallbackQuery().getData(), update.getCallbackQuery().getMessage());
                } catch (Exception e) {
                    Console.output("error message press > ...", true);
                    e.printStackTrace();
                }

            } else if (str.contains("<-")) {
                try {
                    Console.output("button press <", true);
                    deleteMessage(update.getCallbackQuery().getMessage());
                    messageListOfBooks(update.getCallbackQuery().getData(), update.getCallbackQuery().getMessage());
                    Console.output("button good", true);
                } catch (Exception e) {
                    Console.output("error message press < ...", true);
                    e.printStackTrace();
                }
            } else {
                try {
                    deleteMessage(update.getCallbackQuery().getMessage());
                    Console.output("button press number" + update.getCallbackQuery().getMessage().toString(), true);
                    messageAboutBook(update.getCallbackQuery().getMessage().getChatId().toString(),
                            ToplibaParser.fillElements(update.getCallbackQuery().getData()));
                    Console.output("button good", true);
                } catch (Exception e) {
                    Console.output("error message...", true);
                    e.printStackTrace();
                }
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
