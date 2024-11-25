package moviebooking;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Calendar;
import java.time.LocalTime;
import java.util.List;
public class Moviebooking extends JFrame{
    public static JFrame frame;
    public static JPanel panel;
    private static Connection connect() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/db","root","Sai@30.");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static void initializeDatabase() {
        try{
            Connection conn = connect(); 
            Statement stmt = conn.createStatement(); 
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "username varchar(50) NOT NULL UNIQUE, " +
                    "password varchar(50) NOT NULL)";
            stmt.execute(sql);
            String sql1="CREATE TABLE IF (" +
            "    booking_id INT AUTO_INCREMENT PRIMARY KEY," +
               "    movie_name VARCHAR(100)," +
           "    book_date Varchar(50)," +
        "    book_time varchar(50)," +
           "    seats VARCHAR(50)," +
               "    customer_id INT)" ;
            stmt.execute(sql1);
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public  static void main(String[] args) {
        initializeDatabase();
        SwingUtilities.invokeLater(() -> createAndShowLogin());
    }
    private  static JButton[] seatButtons;
    private static JPanel seatPanel;
    private  static void MovieSeatBooking(int id,String movie_name,String book_date,String book_time,JFrame frame,JPanel panel) {
        frame.remove(panel);
        frame.revalidate();
        frame.repaint();
        JPanel panel2=new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        panel2.setBackground(Color.cyan);
        seatPanel = new JPanel();
        seatPanel.setLayout(new GridLayout(5, 5, 10, 10));  // 5x5 grid for seats
        seatPanel.setBackground(Color.cyan);
        // Initialize seat buttons
        seatButtons = new JButton[25];
        for (int i = 0; i < seatButtons.length; i++) {
            seatButtons[i] = new JButton("Seat " + (i + 1));
            seatButtons[i].setBackground(Color.GREEN);  // Default color for available seats
            seatPanel.add(seatButtons[i]);

            // Add action listener for seat selection
            int seatIndex = i;  // Final variable for use in lambda
            seatButtons[i].addActionListener(e -> {
                JButton clickedButton = (JButton) e.getSource();
                if (clickedButton.getBackground() == Color.GREEN) {
                    clickedButton.setBackground(Color.YELLOW);  // Mark as selected
                } else if (clickedButton.getBackground() == Color.YELLOW) {
                    clickedButton.setBackground(Color.GREEN);  // Deselect
                }
            });
        }

        // Fetch booked seats from the database
        List<Integer> bookedSeats = fetchBookedSeats(movie_name,book_date,book_time);
        for (int seatIndex : bookedSeats) {
            seatButtons[seatIndex - 1].setBackground(Color.RED);  // Mark as booked
            seatButtons[seatIndex - 1].setEnabled(false);  // Disable button
        }

        // Add confirm booking button
        JButton confirmButton = new JButton("Confirm Booking");
        confirmButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmButton.addActionListener(e -> confirmBooking(movie_name,book_date,book_time,id,panel,panel2,frame));
        panel2.add(seatPanel);
        panel2.add(confirmButton);
        frame.add(panel2);
        frame.setVisible(true);
    }

    // Static method to fetch booked seats from the database
    private static List<Integer> fetchBookedSeats(String movie_name,String book_date,String book_time) {
        List<Integer> bookedSeats = new ArrayList<>();

        String query = "SELECT seats FROM seat_bookings WHERE movie_name = ? AND book_date = ? AND book_time = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db","root","Sai@30.");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, movie_name);
            pstmt.setString(2, book_date);
            pstmt.setString(3, book_time);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String[] seats = rs.getString("seats").split(",");  // Parse booked seats
                for (String seat : seats) {
                    bookedSeats.add(Integer.parseInt(seat.replaceAll("\\D", "")));  // Extract seat number
                }
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookedSeats;
    }

    // Static method to confirm the booking
    private static void confirmBooking(String movie_name,String book_date,String book_time,int id,JPanel panel1,JPanel panel2,JFrame frame) {
        List<String> selectedSeats = new ArrayList<>();
        for (int i = 0; i < seatButtons.length; i++) {
            if (seatButtons[i].getBackground() == Color.YELLOW) {  // Selected seats
                selectedSeats.add("Seat " + (i + 1));
            }
        }

        if (!selectedSeats.isEmpty()) {
            String bookedSeatsStr = String.join(",", selectedSeats); // Convert list of seats to a comma-separated string
        String currentDate = book_date;
        String currentTime = book_time;

        // MySQL connection and insert query
            String query = "INSERT INTO seat_bookings (movie_name, customer_id, seats, book_date, book_time) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db","root","Sai@30.");
                    PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1,  movie_name);
                preparedStatement.setInt(2, id);
                preparedStatement.setString(3, bookedSeatsStr);
                preparedStatement.setString(4, currentDate);
                preparedStatement.setString(5, currentTime);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Booking successful!");
                    frame.remove(panel2);
                    frame.revalidate();
                    frame.repaint();
                    frame.add(panel1);
                }
               } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error booking seats: " + e.getMessage());
            e.printStackTrace();
        }
       // Add code here to update the database with new bookings.
          // Add code here to update the database with new bookings.
        } else {
            JOptionPane.showMessageDialog(null, "No seats selected!");
        }
    }

    private static void createAndShowLogin() {
        // Create the main frame
        JFrame frame = new JFrame("Movie Ticket booking");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(400, 350);
        frame.getContentPane().setBackground(Color.red);

        frame.setLayout(new GridBagLayout()); // Center content in the frame

        // Create a panel to hold the components
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
        panel.setBackground(Color.cyan);
        // Add components to the panel
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacer

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(usernameLabel);

        JTextField usernameField = new JTextField(15);
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField(15);
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacer

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton loginButton = new JButton("Login");
        JButton signUpButton = new JButton("Sign Up");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginUser(usernameField.getText(), new String(passwordField.getPassword()),frame,panel);
                    }
        });
        signUpButton.addActionListener(e -> createAndShowSignUp());
        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);

        // Add button panel to the main panel
        panel.add(buttonPanel);

        // Add the panel to the frame
        frame.add(panel);

        // Center the frame on screen and make it visible
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void createAndShowSignUp() {
        JFrame frame = new JFrame("Sign Up Page");
        frame.setSize(400, 200);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 5, 5));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e ->signUpUser(usernameField.getText(), new String(passwordField.getPassword())));

        panel.add(submitButton);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void loginUser(String username, String password,JFrame frame,JPanel panel) {
        
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                int id=rs.getInt("id");
                frame.remove(panel);
                frame.revalidate();
                frame.repaint();
        JPanel panel1=new JPanel(new FlowLayout());
        // Create a panel for the search section
        JPanel searchPanel = new JPanel();
        panel1.setBackground(Color.cyan);
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        // Create and add components to the search panel
        JLabel movieLabel = new JLabel("Movie Name:");
        JTextField movieField = new JTextField(20);
        // Create a JComboBox to hold the dates
        JComboBox<String> dateComboBox = new JComboBox<>();
        
        // Get today's date and the next 4 dates
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        
        // Add the next 5 dates to the combo box
        for (int i = 0; i < 5; i++) {
            String formattedDate = dateFormat.format(calendar.getTime());
            dateComboBox.addItem(formattedDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);  // Move to the next day
        }
        JLabel timeLabel = new JLabel("Time:");
        JLabel ldate = new JLabel("Date:");
       // Define the time slots in AM/PM format
        // Define the available time slots
        String[] timeSlots = {"9:00 AM", "1:00 PM", "5:00 PM", "10:00 PM"};
        
        // Get current time
        LocalTime currentTime = LocalTime.now();
         
        // List to store valid time slots
        List<String> validTimeSlots = new ArrayList<>();

        // Convert time slots to LocalTime and compare with current time
        for (String timeSlot : timeSlots) {
            // Parse the time slots and convert them to LocalTime objects
            LocalTime time = parseTimeSlot(timeSlot);
            
            // Only add the time to the list if it's after the current time
            //if (time.isAfter(currentTime)) {
                validTimeSlots.add(timeSlot);
            //}
        }

        // Create a JComboBox with the valid time slots
        JComboBox<String> timeField = new JComboBox<>(validTimeSlots.toArray(new String[0]));


        JButton searchButton = new JButton("Search");
       
        searchPanel.add(movieLabel);
        searchPanel.add(movieField);
        searchPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        searchPanel.add(ldate);
        searchPanel.add(dateComboBox);
        searchPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        searchPanel.add(timeLabel);
        searchPanel.add(timeField);
        searchPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        searchPanel.add(searchButton);
       searchPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        // Add an area for suggestions (using a JList for example)
        DefaultListModel<String> suggestionsModel = new DefaultListModel<>();
        JList<String> suggestionsList = new JList<>(suggestionsModel);
        JScrollPane suggestionsScrollPane = new JScrollPane(suggestionsList);
       
        // Sample movie suggestions
        String[] sampleMovies = {"Inception", "The Matrix", "Titanic", "Avatar"};
        for (String movie : sampleMovies) {
            suggestionsModel.addElement(movie);
        }
       
        // Add listener for movie field to show suggestions
        movieField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSuggestions();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSuggestions();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSuggestions();
            }

            private void updateSuggestions() {
                String input = movieField.getText().toLowerCase();
                suggestionsModel.clear();
                for (String movie : sampleMovies) {
                    if (movie.toLowerCase().contains(input)) {
                        suggestionsModel.addElement(movie);
                    }
                }
            }
        });
       
        // Add action to search button
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String movieName = movieField.getText();
                String date = (String)dateComboBox.getSelectedItem();
                String time = (String)timeField.getSelectedItem();
          
                if (movieName.isEmpty() || date.isEmpty() || time.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please fill out all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    MovieSeatBooking(id,movieName,date,time,frame,panel1);
                }
            }
        });
       searchPanel.setBackground(Color.cyan);
        // Add components to the frame
        panel1.add(searchPanel);
        panel1.add(suggestionsScrollPane);
        frame.add(panel1);
       
        // Make the frame visible
        frame.setVisible(true);
            }
            else{
                JOptionPane.showMessageDialog(null,"Invalid username or password","Error",JOptionPane.ERROR_MESSAGE);
            }
    }
    catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void signUpUser(String username, String password) {
        String query = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Sign Up Successful!");
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) {  // SQLite constraint violation for UNIQUE
                JOptionPane.showMessageDialog(null, "Username already taken", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
            }
        }
    }
    private static LocalTime parseTimeSlot(String timeSlot) {
        switch (timeSlot) {
            case "9:00 AM":
                return LocalTime.of(9, 0);
            case "1:00 PM":
                return LocalTime.of(13, 0);
            case "5:00 PM":
                return LocalTime.of(17, 0);
            case "10:00 PM":
                return LocalTime.of(22, 0);
            default:
                throw new IllegalArgumentException("Invalid time slot");
        }
}
}
