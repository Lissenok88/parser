package com.lyudmila.topliba;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Messages extends ToplibaBot{
    private ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

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

    public void messageListOfBooks(String pageStr, Message message, HashMap<String, ArrayList<BookInformation>> foundBooks) {
        int page = pageCalculation(pageStr);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        Console.output("page text do: " + pageStr, true);
        String search = searchName(pageStr);
        if (search.equals("")) search = message.getText();
        Console.output("string Name past: " + search, true);
        String result = readPage(page, foundBooks, search);
        sendMessage.setText(result);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(readButton(page, foundBooks.get(search), search));
        inlineKeyboardMarkup.setKeyboard(rowList);
        try {
            sendMessage.setText(result);
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            execute(sendMessage);
        } catch (Exception e) {
            Console.output(e.getMessage(), true);
        }
    }

    public void editMessageListOfBooks(String pageStr, Message message, HashMap<String, ArrayList<BookInformation>> foundBooks) {

        int page = pageCalculation(pageStr);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        Console.output("page text do: " + pageStr, true);
        String search = searchName(pageStr);
        if (search.equals("")) search = message.getText();
        Console.output("string Name past: " + search, true);
        String result = readPage(page, foundBooks, search);
        sendMessage.setText(result);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(readButton(page, foundBooks.get(search), search));
        inlineKeyboardMarkup.setKeyboard(rowList);
        EditMessageText editMessageText = new EditMessageText();

        try {
            editMessageText.setChatId(message.getChatId().toString());
            editMessageText.setMessageId(message.getMessageId());
            editMessageText.setText(result);
            editMessageText.setReplyMarkup(inlineKeyboardMarkup);
            execute(editMessageText);
        } catch (Exception e) {
            Console.output(e.getMessage(), true);
        }
    }

    private String searchName(String string) {
        String name = "";
        int index = string.indexOf(">");
        if (index != -1) {
            name = string.substring(0, index - 1);
        } else {
            index = string.indexOf("<");
            if (index != -1) {
                name = string.substring(index + 2);
            }
        }
        Console.output("string in SearchName: " + name);
        return name;
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

    private String readPage(int page, HashMap<String, ArrayList<BookInformation>> search, String message) {
        ArrayList<BookInformation> foundBooks = new ArrayList<>();
        foundBooks.addAll(search.get(message));
        StringBuilder result = new StringBuilder("[Topliba](https://topliba.com/)");
        result.append(System.lineSeparator().repeat(2));
        result.append("Поиск: " + message);
        result.append(System.lineSeparator().repeat(2));
        result.append("*Страница " + page + " из " + pageCountCalculation(foundBooks.size()) + "*");
        result.append(System.lineSeparator().repeat(2));
        int index = page * 5 - 5;
        for (int i = 0; i < 5; i++) {
            if (index < foundBooks.size() && foundBooks.get(index).getUrl() != null) {
                result.append("*" + foundBooks.get(index).getPosition() + ".* " + foundBooks.get(index).getTitle());
                result.append("[  >>](" + foundBooks.get(index).getUrl() + ")");
                result.append(System.lineSeparator().repeat(2));
                index++;
                if (index == foundBooks.size())
                    break;
            }
        }
        return result.toString();
    }

    private List<InlineKeyboardButton> readButton(int page, ArrayList<BookInformation> foundBooks, String message) {
        int index = page * 5 - 5;
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        if (page != 1) {
            int position = page - 1;
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(position + "<-");
            button.setCallbackData(position + "<-" + message);
            keyboardButtonsRow.add(button);
            Console.output("button: <- " + position, true);
        }
        for (int i = 0; i < 5; i++) {
            if (index < foundBooks.size() && foundBooks.get(index).getUrl() != null) {
                int iterator = index + 1;
                InlineKeyboardButton button2 = new InlineKeyboardButton(Integer.toString(iterator));
                button2.setCallbackData(foundBooks.get(index).getUrl());
                keyboardButtonsRow.add(button2);
                Console.output("button: " + foundBooks.get(index).getPosition() + "url: " +
                        foundBooks.get(index).getUrl(), true);
                index++;
                if (index == foundBooks.size())
                    break;
            }
        }
        if (page != pageCountCalculation(foundBooks.size())) {
            int nextPage = page + 1;
            InlineKeyboardButton button = new InlineKeyboardButton("->" + nextPage);
            button.setCallbackData(message + "->" + nextPage);
            keyboardButtonsRow.add(button);
            Console.output("button: -> " + nextPage, true);
        }
        return keyboardButtonsRow;
    }

    public int pageCountCalculation(int length) {
        int countPages = length / 5;
        Console.output(Integer.toString(countPages), true);
        if (countPages % 5 != 0)
            countPages = countPages + 1;
        Console.output(Integer.toString(countPages), true);
        return countPages;
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
}
