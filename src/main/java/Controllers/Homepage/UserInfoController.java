package Controllers.Homepage;

import Models.*;
import Util.SQL.QueryFactory.DeleteQueryFactory;
import Util.SQL.QueryFactory.InsertQueryFactory;
import Util.SQL.QueryFactory.SelectQueryFactory;
import Util.SQL.QueryStatements.DeleteQueries.DeleteQuery;
import Util.SQL.QueryStatements.InsertQueries.InsertQuery;
import Util.SQL.QueryStatements.SelectQueries.SelectQuery;
import com.github.javafaker.Faker;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserInfoController {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label avatarLabel;
    @FXML
    private TableView<Beer> brewTableView;
    @FXML
    private TableColumn<Beer, String> brewColumn;
    @FXML
    private TableColumn<Beer, String> breweryColumn;
    @FXML
    private TableView<Beer> beerDbTable;
    @FXML
    private TableColumn<Beer, String> beerDbBrewName;
    @FXML
    private TableColumn<Beer, String> beerDbBrewery;
    @FXML
    private TableView<MatchData> matchTable;
    @FXML
    private TableColumn<MatchData, String> userMatchColumn;
    @FXML
    private TableColumn<MatchData, String> brewMatchColumn;
    @FXML
    private ImageView avatarImage;
    @FXML
    private Label chucknorrisLabel;
    @FXML
    private TextField breweryTextField;
    @FXML
    private TextField brewnameTextField;
    @FXML
    private Button addBeerButton;
    @FXML
    private Button addUserBeerButton;
    @FXML
    private Label matchFirstName;
    @FXML
    private Label matchLastName;
    @FXML
    private TableView<Beer> matchBrewTable;
    @FXML
    private TableColumn<Beer, String> matchBrew;
    @FXML
    private TableColumn<Beer, String> matchBrewery;
    @FXML
    private ImageView matchAvatar;

    private ObservableList<Beer> userBeerData = FXCollections.observableArrayList();
    private ObservableList<Beer> beerDbData = FXCollections.observableArrayList();
    private ObservableList<Beer> matchBrewData = FXCollections.observableArrayList();
    private Matches matchesData = new Matches();

    private int chuckNorrisDuration = 15;
    private String username;
    private String avatarName;
    private String firstname;
    private String lastname;
    private Faker f = new Faker();

    public UserInfoController(User u) {
        setUsername(u.getUsername());
        setAvatarName(AvatarMapping.getReverseMapping(u.getAid()));
        setFirstname(u.getFirst());
        setLastname(u.getLast());
    }

    @FXML
    public void initialize() throws IOException {
        //init column cell objects
        brewColumn.setCellValueFactory(cellData -> cellData.getValue().brewNameProperty());
        breweryColumn.setCellValueFactory(cellData -> cellData.getValue().breweryProperty());
        beerDbBrewName.setCellValueFactory(cellData -> cellData.getValue().brewNameProperty());
        beerDbBrewery.setCellValueFactory(cellData -> cellData.getValue().breweryProperty());
        userMatchColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        usernameLabel.setText(firstname + " " + lastname); //???
        brewMatchColumn.setCellValueFactory(cellData -> cellData.getValue().beerProperty());
        Image image = new Image(AvatarMapping.getPhotoPathMapping(avatarName));
        avatarImage.setImage(image);
        matchFirstName.setText("");
        matchLastName.setText("");
        chucknorrisLabel.setText(f.chuckNorris().fact());
        matchTable.setPlaceholder(new Label("Add beers, get matches!"));
        brewTableView.setPlaceholder(new Label("No favorited beers!"));
        matchBrewTable.setPlaceholder(new Label("Click on a match below to see their details!"));
        initChuckNorrisFacts();
        populateUserBeers(username);
        populateBeerDb();
        populateMatches(username);
        brewTableView.setItems(userBeerData);
        beerDbTable.setItems(beerDbData);
        matchTable.setItems(matchesData.getMatches());
        matchBrew.setCellValueFactory(cellData -> cellData.getValue().brewNameProperty());
        matchBrewery.setCellValueFactory(cellData -> cellData.getValue().breweryProperty());
        matchTable.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) -> {
            matchBrewData.removeAll();
            matchBrewTable.getItems().clear();

            if (newVal != null)
            {
                for (Beer beer : newVal.getMatchedBeers())
                {
                    matchBrewData.add(beer);
                }
                matchBrewTable.setItems(matchBrewData);
                User mu = newVal.getMatchedUser();
                matchFirstName.setText(mu.getFirst());
                matchLastName.setText(mu.getLast());
                String a = AvatarMapping.getReverseMapping(mu.getAid());
                Image mImage = new Image(AvatarMapping.getPhotoPathMapping(a));
                matchAvatar.setImage(mImage);
            }
            else
            {
                matchBrewData.removeAll();
                matchBrewTable.getItems().clear();
                matchBrewTable.setItems(matchBrewData);
                matchAvatar.setImage(null);
                matchFirstName.setText("");
                matchLastName.setText("");
            }
        });

        addBeerButton.setOnAction( event -> {
            String brewery = breweryTextField.getText();
            String brewName = brewnameTextField.getText();
            Beer b = new Beer("DEFAULT", brewery, brewName); //dont need BID since auto increment on insert
            if (brewery.isEmpty() || brewName.isEmpty())
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter brewery and brew name to add beer\n");
                alert.showAndWait();
            }
            //TODO say goodbye to integrity
            else
            {
                InsertQuery insertIntoBeers = InsertQueryFactory.getQuery(Tables.beers);
                insertIntoBeers.execute(b);
                b.setBid(Integer.toString(userBeerData.size()));
                beerDbData.add(b);
                beerDbTable.setItems(beerDbData);
            }
        });
        addUserBeerButton.setOnAction(event -> {
            ChoiceDialog<Beer> dialog = new ChoiceDialog<Beer>(beerDbData.get(0), beerDbData);
            dialog.setTitle("Add Beer to Favorites");
            dialog.setHeaderText("Add Beer to Favorites");
            dialog.setContentText("Choose beer");

            // Remove beers from dropdown that are already favorited
            List<Beer> list = dialog.getItems();
            list.removeIf(b -> userBeerData.contains(b));

            Optional<Beer> result = dialog.showAndWait();
            if (result.isPresent()){
                if (!(userBeerData.contains(result.get()))) { //redundant check
                    BeerChoice b = new BeerChoice(username, Integer.parseInt(result.get().getBid()), "DEFAULT");
                    InsertQuery insertIntoFavorites = InsertQueryFactory.getQuery(Tables.beer_choices);
                    insertIntoFavorites.execute(b);
                    userBeerData.add(result.get());
                    brewTableView.setItems(userBeerData);
                    CheckForNewMatches(result.get().getBid());
                }
            }
        });

        brewTableView.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle( final KeyEvent keyEvent )
            {
                Beer selectedItem = brewTableView.getSelectionModel().getSelectedItem();
                if (selectedItem != null)
                {
                    if (keyEvent.getCode().equals(KeyCode.DELETE) || keyEvent.getCode().equals(KeyCode.BACK_SPACE))
                    {
                        // Confirm removing beer from favorites
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Delete");
                        alert.setHeaderText("Deleting Beer from Favorites");
                        alert.setContentText("Are you sure you want to delete " + selectedItem.toString() + " from favorites?");
                        Optional<ButtonType> result = alert.showAndWait();

                        if (result.get() == ButtonType.OK)
                        {
                            //delete beer choice
                            BeerChoice bc = new BeerChoice(username, new Integer(selectedItem.getBid()), "DEFAULT");
                            DeleteQuery deleteChoiceQuery = DeleteQueryFactory.getQuery(Tables.beer_choices);
                            deleteChoiceQuery.execute(bc);
                            brewTableView.getItems().removeAll(selectedItem);

                            DeleteQuery deleteMatchQuery = DeleteQueryFactory.getQuery(Tables.matches);
                            deleteMatchQuery.execute(bc);

                            reloadMatches(username);
                        }
                    }
                }
            }
        });
    }

    private void CheckForNewMatches(String newBeer)
    {
        SelectQuery selectQuery = SelectQueryFactory.getQuery(Tables.beer_choices);
        ResultSet rs = selectQuery.execute(username, true);
        try {
            while (rs.next()) {
                if (rs.getString("bid").equals(newBeer))
                {
                    MatchedUser match = new MatchedUser(username, rs.getString("username"), newBeer, "DEFAULT");
                    InsertQuery insertQuery = InsertQueryFactory.getQuery(Tables.matches);
                    insertQuery.execute(match);
                    reloadMatches(username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void parseResultSetBeer(ResultSet rs, ObservableList<Beer> list) throws SQLException {
        String beerID = rs.getString("bid");
        String brewName = rs.getString("brewname");
        String brewery = rs.getString("brewery");
        Beer beer = new Beer(beerID, brewery, brewName);
        list.add(beer);
    }

    private void populateBeerDb() {
        SelectQuery selectFromBeers = SelectQueryFactory.getQuery(Tables.beers);
        ResultSet rs = selectFromBeers.execute("> -1", false);
        try {
            while (rs.next()) {
                parseResultSetBeer(rs, beerDbData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateUserBeers(String username) {
        ArrayList<Integer> bidData = new ArrayList<>();
        SelectQuery selectBeerChoice = SelectQueryFactory.getQuery(Tables.beer_choices);
        ResultSet beerResults = selectBeerChoice.execute(username, false);
        //find all the beer choices for this user
        try {
            while (beerResults.next()) {
                String bid = beerResults.getString("bid");
                bidData.add(new Integer(bid));
            }
            SelectQuery selectFromBeers = SelectQueryFactory.getQuery(Tables.beers);
            //populate the beer model for user beer in this users beer choice list
            for (Integer bid : bidData) {
                ResultSet rs = selectFromBeers.execute("="+bid.toString(), false);
                if (rs.next()) {
                    parseResultSetBeer(rs, userBeerData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void reloadMatches(String username)
    {
        matchesData = new Matches();
        populateMatches(username);
        matchTable.setItems(matchesData.getMatches());
    }

    private void populateMatches(String username) {

        List<MatchedUser> matches = new ArrayList<>();
        SelectQuery selectMatches = SelectQueryFactory.getQuery(Tables.matches);
        ResultSet rs = selectMatches.execute(username, false);

        //find all the matches for this user
        try {
            while (rs.next()) {
                String mid = rs.getString("mid");
                String user = username;
                String match = rs.getString("matched_user");
                String bid = rs.getString("bid");

                MatchedUser m = new MatchedUser(user, match, bid, mid);
                matches.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (MatchedUser match : matches)
        {
            // Get user associated with match
            SelectQuery selectUser = SelectQueryFactory.getQuery(Tables.users);
            ResultSet user = selectUser.execute(new UserPassPair(match.getMatch(), ""), true);
            User mUser = null;
            try
            {
                while (user.next())
                {
                    String un = user.getString("username");
                    String first = user.getString("first");
                    String last = user.getString("last");
                    String aid = user.getString("aid");
                    mUser = new User(un, "", first, last, Integer.parseInt(aid));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Get beer associated with match
            SelectQuery selectBeer = SelectQueryFactory.getQuery(Tables.beers);
            ResultSet beer = selectBeer.execute("="+match.getBid(), false);
            Beer newBeer = null;
            try
            {
                while (beer.next())
                {
                    String bid = beer.getString("bid");
                    String brewery = beer.getString("brewery");
                    String brewname = beer.getString("brewname");
                    newBeer = new Beer(bid, brewery, brewname);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Add user to matches list
            if (mUser != null && newBeer != null)
            {
                matchesData.addMatch(mUser, newBeer);
            }
        }
    }

    private void initChuckNorrisFacts() {
        Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(chuckNorrisDuration), event -> chucknorrisLabel.setText(f.chuckNorris().fact())));
        fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
        fiveSecondsWonder.play();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarName() {
        return avatarName;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }
}
