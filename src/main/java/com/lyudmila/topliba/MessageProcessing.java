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

public class MessageProcessing extends ToplibaBot {
    private final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    private static final String SEARCH_BUTTON_NAME = "Найти книгу.";
    private static final String DOWNLOAD_BUTTON_NAME = "fb2";
    private static final int COUNT_OF_BOOKS_ON_PAGE = 5;
    private static final int FIRST_PAGE = 1;

    public void message(Message message, String update) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(true);
            sendMessage.setChatId(message.getChatId().toString());
            setButton(sendMessage);
            sendMessage.setText(update);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            Console.output(e.getMessage(), true);
        }
    }

    public void messageListOfBooks(Message message, ArrayList<BookInformation> foundBooks) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        String messageText = formPageTextListOfBooks(FIRST_PAGE, foundBooks, message.getText());
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(formPageButtonsListOfBooks(FIRST_PAGE, foundBooks, message.getText()));
        inlineKeyboardMarkup.setKeyboard(rowList);
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            sendMessage.enableMarkdown(true);
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText(messageText);
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            execute(sendMessage);
        } catch (Exception e) {
            Console.output(e.getMessage(), true);
        }
    }

    public void editMessageListOfBooks(String textButton, Message message,
                                       HashMap<String, ArrayList<BookInformation>> listRequest) {
        int page = getPage(textButton);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        String nameSearch = getNameSearch(textButton);
        String textMessage = formPageTextListOfBooks(page, listRequest.get(nameSearch), nameSearch);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(formPageButtonsListOfBooks(page, listRequest.get(nameSearch), nameSearch));
        inlineKeyboardMarkup.setKeyboard(rowList);
        try {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(message.getChatId().toString());
            editMessageText.setMessageId(message.getMessageId());
            editMessageText.enableMarkdown(true);
            editMessageText.setText(textMessage);
            editMessageText.setReplyMarkup(inlineKeyboardMarkup);
            execute(editMessageText);
        } catch (Exception e) {
            Console.output(e.getMessage(), true);
        }
    }


    private String formPageTextListOfBooks(int page, ArrayList<BookInformation> foundBooks, String nameSearch) {
        StringBuilder pageText = new StringBuilder();
        pageText.append("*Поиск: " + nameSearch + "*");
        pageText.append(System.lineSeparator().repeat(2));
        pageText.append("*Страница " + page + " из " + getCountPage(foundBooks.size()) + "*");
        pageText.append(System.lineSeparator().repeat(2));
        int position = page * COUNT_OF_BOOKS_ON_PAGE - COUNT_OF_BOOKS_ON_PAGE;
        for (int i = 0; i < COUNT_OF_BOOKS_ON_PAGE; i++) {
            if (position < foundBooks.size() && foundBooks.get(position).getUrl() != null) {
                pageText.append("*" + foundBooks.get(position).getPosition() + ".* " + foundBooks.get(position).getTitle());
                pageText.append(System.lineSeparator().repeat(2));
                position++;
                if (position == foundBooks.size())
                    break;
            }
        }
        return pageText.toString();
    }

    private List<InlineKeyboardButton> formPageButtonsListOfBooks(int page, ArrayList<BookInformation> foundBooks,
                                                                  String nameSearch) {
        int position = page * COUNT_OF_BOOKS_ON_PAGE - COUNT_OF_BOOKS_ON_PAGE;
        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
        if (page != FIRST_PAGE) {
            addButtonBack(page, nameSearch, keyboardButtons);
        }
        addButtonsBookNumbers(position, foundBooks, keyboardButtons);
        if (page != getCountPage(foundBooks.size())) {
            addButtonForward(page, nameSearch, keyboardButtons);
        }
        return keyboardButtons;
    }

    private void addButtonBack(int page, String nameSearch, List<InlineKeyboardButton> keyboardButtons) {
        int previousPage = page - 1;
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText(previousPage + "<-");
        buttonBack.setCallbackData(previousPage + "<-" + nameSearch);
        keyboardButtons.add(buttonBack);
    }

    private void addButtonsBookNumbers(int position, ArrayList<BookInformation> foundBooks,
                                       List<InlineKeyboardButton> keyboardButtons) {
        for (int i = 0; i < COUNT_OF_BOOKS_ON_PAGE; i++) {
            if (position < foundBooks.size() && foundBooks.get(position).getUrl() != null) {
                int bookNumber = position + 1;
                InlineKeyboardButton buttonBookNumber = new InlineKeyboardButton(Integer.toString(bookNumber));
                buttonBookNumber.setCallbackData(foundBooks.get(position).getUrl());
                keyboardButtons.add(buttonBookNumber);
                position++;
                if (position == foundBooks.size())
                    break;
            }
        }
    }

    private void addButtonForward(int page, String nameSearch, List<InlineKeyboardButton> keyboardButtons) {
        int nextPage = page + 1;
        InlineKeyboardButton buttonForward = new InlineKeyboardButton("->" + nextPage);
        buttonForward.setCallbackData(nameSearch + "->" + nextPage);
        keyboardButtons.add(buttonForward);
    }

    public void messageAboutBook(String message, BookInformation bookInformation) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        String textMassage = formMessageTextAboutOfBook(bookInformation);
        try {
            if (!bookInformation.getUrlFb2().isEmpty()) {
                inlineKeyboardMarkup.setKeyboard(formMessageButtonAboutOfBook(bookInformation));
                sendMessage.setText(textMassage);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                execute(sendMessage);
            } else {
                setButton(sendMessage);
                sendMessage.setText(textMassage);
                execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            Console.output(e.getMessage(), true);
        }
    }

    private String formMessageTextAboutOfBook(BookInformation bookInformation) {
        StringBuilder textMessage = new StringBuilder("*" + bookInformation.getTitle() + "*");
        textMessage.append(System.lineSeparator().repeat(2));
        textMessage.append(bookInformation.getDescription());
        textMessage.append(System.lineSeparator().repeat(2));
        textMessage.append("_" + bookInformation.getFragment() + "_");
        return textMessage.toString();
    }

    private List<List<InlineKeyboardButton>> formMessageButtonAboutOfBook(BookInformation bookInformation) {
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton buttonDownload = new InlineKeyboardButton();
        buttonDownload.setText(DOWNLOAD_BUTTON_NAME);
        buttonDownload.setCallbackData(bookInformation.getUrlFb2());
        keyboardButtonsRow.add(buttonDownload);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow);
        return rowList;
    }

    private int getCountPage(int countBooks) {
        int countPages = countBooks / COUNT_OF_BOOKS_ON_PAGE;
        if (countPages % COUNT_OF_BOOKS_ON_PAGE != 0)
            countPages = countPages + 1;
        return countPages;
    }

    private String getNameSearch(String textButton) {
        String nameSearch = "";
        int index = textButton.indexOf(">");
        if (index != -1) {
            nameSearch = textButton.substring(0, index - 1);
        } else {
            index = textButton.indexOf("<");
            if (index != -1) {
                nameSearch = textButton.substring(index + 2);
            }
        }
        return nameSearch;
    }

    private int getPage(String textButton) {
        int page = 0;
        int index = textButton.indexOf(">");
        if (index != -1) {
            String pageNumber = textButton.substring(index + 1);
            if (!pageNumber.isEmpty()) page = Integer.parseInt(pageNumber);
        } else {
            index = textButton.indexOf("<");
            if (index != -1) {
                String pageNumber = textButton.substring(0, index);
                if (!pageNumber.isEmpty()) page = Integer.parseInt(pageNumber);
            }
        }
        return page;
    }

    public void deleteMessage(Message message) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(message.getChatId()));
        deleteMessage.setMessageId(message.getMessageId());
        deleteMessage.getChatId();
        deleteMessage.getMessageId();
        try {
            execute(deleteMessage);
        } catch (TelegramApiException ex) {
            Console.output(ex.getMessage(), true);
        }
    }

    private void setButton(SendMessage sendMessage) {
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton(SEARCH_BUTTON_NAME));
        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }
}
