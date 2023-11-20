package app;

import data_access.*;
import entity.Player;
import entity.Song;
import interface_adapter.Chat.ChatViewModel;
import interface_adapter.PlayerGuess.PlayerGuessViewModel;
import interface_adapter.SendMessage.SendMessageLoggerModel;
import interface_adapter.SingerChoose.SingerChooseState;
import interface_adapter.SingerChoose.SingerChooseViewModel;
import interface_adapter.SingerSing.SingerSingViewModel;
import interface_adapter.UpdateScore.UpdateScoreViewModel;
import interface_adapter.ViewManagerModel;
import logger.MessageLogger;
import org.json.JSONObject;
import use_case.UpdateScore.UpdateScoreDataAccessInterface;
import use_case.UpdateScore.UpdateScoreInputBoundary;
import use_case.UpdateScore.UpdateScoreInteractor;
import use_case.UpdateScore.UpdateScoreRoundStateDataAccessInterface;
import view.*;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Build the main program window, the main panel containing the
        // various cards, and the layout, and stitch them together.

        // The main application window.
        JFrame application = new JFrame("AcappellaFella");
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();

        // The various View objects. Only one view is visible at a time.
        JPanel views = new JPanel(cardLayout);
        application.add(views);

        // This keeps track of and manages which view is currently showing.
        ViewManagerModel viewManagerModel = new ViewManagerModel();
        new ViewManager(views, cardLayout, viewManagerModel);

        // Message logger model
        SendMessageLoggerModel sendMessageLoggerModel = new SendMessageLoggerModel();

        // View Models
        SingerChooseViewModel singerChooseViewModel = new SingerChooseViewModel();
        ChatViewModel chatViewModel = new ChatViewModel();
        SingerSingViewModel singerSingViewModel = new SingerSingViewModel();
        PlayerGuessViewModel playerGuessViewModel = new PlayerGuessViewModel();
        UpdateScoreViewModel updateScoreViewModel = new UpdateScoreViewModel();


        // DAOs
        InMemoryGameStateGameStateDataAccessObject gameStateDAO = new InMemoryGameStateGameStateDataAccessObject();
        InMemoryRoundStateDataAccessObject roundStateDAO = new InMemoryRoundStateDataAccessObject();
        InMemoryMessageHistoryDataAccessObject messageHistoryDAO = new InMemoryMessageHistoryDataAccessObject();
        InMemoryPlayerDataAccessObject playerDAO = new InMemoryPlayerDataAccessObject();
        InMemoryScoreboardDataAccessObject scoreboardDAO = new InMemoryScoreboardDataAccessObject();

        //
        UpdateScoreInteractor updateScoreInteractor = UpdateScoreUseCaseFactory.create(scoreboardDAO, roundStateDAO, updateScoreViewModel);

        // Message logger
        MessageLogger messageLogger = MessageLoggerUseCaseFactory.create(messageHistoryDAO, playerDAO, sendMessageLoggerModel, chatViewModel, gameStateDAO, roundStateDAO, updateScoreInteractor);

        /*
         todo remove later
         temp init for singerChooseViewModel
         needs to go in the RunGame use case somewhere
        */
        SingerChooseState singerChooseState = singerChooseViewModel.getState();
        Song song1 = new Song("Queen", "Don't Stop Me now");
        Song song2 = new Song("Queen", "Bohemian Rhapsody");
        Song song3 = new Song("Queen", "Another One Bites The Dust");
        singerChooseState.setSong1(song1);
        singerChooseState.setSong2(song2);
        singerChooseState.setSong3(song3);
        singerChooseViewModel.setState(singerChooseState);

        // todo remove later
        roundStateDAO.addRound();

        // set a song for testing
        roundStateDAO.getCurrentRoundState().setSong(song1);

        // todo remove later
        messageLogger.setChannel("1168619453492236424");

        // todo remove later
        Player me = new Player();
        me.setName("Brandon");
        gameStateDAO.getGameState().setMainPlayer(me);
        gameStateDAO.addPlayer(me);
        playerDAO.save(me);

        Player you = new Player();
        you.setName("eric");
        gameStateDAO.addPlayer(you);
        playerDAO.save(you);

        scoreboardDAO.addPlayer(me);
        scoreboardDAO.addPlayer(you);

        // Views

        SingerChooseView singerChooseView = SingerChooseUseCaseFactory.create(viewManagerModel, singerChooseViewModel, roundStateDAO, singerSingViewModel);
        ChatView chatView = ChatUseCaseFactory.create(gameStateDAO, chatViewModel, sendMessageLoggerModel, playerGuessViewModel, gameStateDAO, roundStateDAO);
        ScoreboardView scoreboardView = ScoreboardViewBuilder.createView(updateScoreViewModel);
        PlayerGuessView playerGuessView = PlayerGuessViewBuilder.createView(scoreboardView, chatView, playerGuessViewModel);

        views.add(singerChooseView, singerChooseView.viewName);
        // Keep this line commented out because otherwise the ChatView will not be added properly to the playerGuessView
        // views.add(chatView, chatView.viewName);
        views.add(playerGuessView, playerGuessView.viewName);

        views.add(scoreboardView, scoreboardView.viewName);

        viewManagerModel.setActiveView(scoreboardView.viewName);
        viewManagerModel.firePropertyChanged();

        application.pack();
        application.setVisible(true);

        // Demonstrate data access object functionality by retrieving three distinct songs
        String accessToken = APIDataAccessObject.requestAccessToken();
        JSONObject playlistData = APIDataAccessObject.requestPlaylistData(accessToken, "37i9dQZF1DX5Ejj0EkURtP");
        System.out.println(playlistData);
        Song songOne = APIDataAccessObject.getSong(playlistData, 1);
        Song songTwo = APIDataAccessObject.getSong(playlistData, 2);
        Song songThree = APIDataAccessObject.getSong(playlistData, 3);
        System.out.println(songOne.toString());
        System.out.println(songTwo.toString());
        System.out.println(songThree.toString());



    }
}