package use_case.CheckGuess;

import entity.Message;

public class CheckGuessOutputData {
    private Message message;


    public CheckGuessOutputData(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
