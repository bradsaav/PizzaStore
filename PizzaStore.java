/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");
         String authorisedUser = null; // Move outside the loop to persist session

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2:  
                  if (authorisedUser == null) { // Only allow login if not already logged in
                     authorisedUser = LogIn(esql);
                     if (authorisedUser != null) {
                        System.out.println("Welcome, " + authorisedUser + "!");
                     }
                  } else {
                     System.out.println("Already logged in as " + authorisedUser);
                  }
                  break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: updateProfile(esql, authorisedUser); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql, authorisedUser); break;
                   case 5: viewAllOrders(esql, authorisedUser); break;
                   case 6: viewRecentOrders(esql, authorisedUser); break;
                   case 7: viewOrderInfo(esql, authorisedUser); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql, authorisedUser); break;
                   case 10: updateMenu(esql, authorisedUser); break;
                   case 11: updateUser(esql, authorisedUser); break;



                   case 20: System.out.println("Logging out " + authorisedUser);
                   authorisedUser = null; // Logout user
                   usermenu = false; // Exit user menu
                   break;
                   default : 
                   System.out.println("Unrecognized choice!"); 
                   break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql){
      try {
         System.out.print("Enter login (username): ");
         String login = in.readLine();
         
         System.out.print("Enter password: ");
         String password = in.readLine();
         
         System.out.print("Enter phone number: ");
         String phone = in.readLine();
 
         // Check if the username already exists
         String checkUserQuery = String.format("SELECT * FROM Users WHERE login='%s';", login);
         int userExists = esql.executeQuery(checkUserQuery);
         
         if (userExists > 0) {
             System.out.println("Username already exists. Please choose a different one.");
             return;
         }
 
         // Insert the new user into the database
         String insertQuery = String.format(
             "INSERT INTO Users (login, password, phonenum, role, favoriteitems) VALUES ('%s', '%s', '%s', 'Customer', NULL);",
             login, password, phone
         );
 
         esql.executeUpdate(insertQuery);
         System.out.println("User registered successfully!");
 
     } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
     }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
       try {
        System.out.print("Enter login: ");
        String login = in.readLine();

        System.out.print("Enter password: ");
        String password = in.readLine();

        // Query to verify user credentials
        String query = String.format(
            "SELECT * FROM Users WHERE login='%s' AND password='%s';",
            login, password
        );

        int userNum = esql.executeQuery(query);
        if (userNum > 0) {
            System.out.println("Login successful!");
            return login; // Return the logged-in user's login
        } else {
            System.out.println("Invalid login or password.");
            return null;
        }
    } catch (Exception e) {
        System.err.println(e.getMessage());
        return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql, String authorisedUser) {
      try {
         System.out.println("Your Profile Information:");
         String query = String.format("SELECT login, favoriteItems, phoneNum, role FROM Users WHERE login = '%s';", authorisedUser);
         esql.executeQueryAndPrintResult(query);
     } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
     }
   }
   public static void updateProfile(PizzaStore esql, String authorisedUser) {
      try {
         boolean updating = true;
         while (updating) {
             System.out.println("UPDATE PROFILE OPTIONS:");
             System.out.println("1. Change Favorite Item");
             System.out.println("2. Change Phone Number");
             System.out.println("3. Change Password");
             System.out.println("4. Go Back");
 
             switch (readChoice()) {
                 case 1:
                     System.out.print("Enter new favorite item: ");
                     String newFav = in.readLine().trim();
                     String updateFavQuery = String.format("UPDATE Users SET favoriteItems = E'%s' WHERE login = '%s';", newFav.replace("'", "''"), authorisedUser);
                     esql.executeUpdate(updateFavQuery);
                     System.out.println("Favorite item updated successfully!");
                     break;
                 case 2:
                     System.out.print("Enter new phone number: ");
                     String newPhone = in.readLine().trim();
                     String updatePhoneQuery = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s';", newPhone, authorisedUser);
                     esql.executeUpdate(updatePhoneQuery);
                     System.out.println("Phone number updated successfully!");
                     break;
                 case 3:
                     System.out.print("Enter new password: ");
                     String newPass = in.readLine().trim();
                     String updatePassQuery = String.format("UPDATE Users SET password = '%s' WHERE login = '%s';", newPass, authorisedUser);
                     esql.executeUpdate(updatePassQuery);
                     System.out.println("Password updated successfully!");
                     break;
                 case 4:
                     updating = false;
                     break;
                 default:
                     System.out.println("Invalid choice. Try again.");
                     break;
             }
         }
     } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
     }
   }
   public static void viewMenu(PizzaStore esql) {
      try {
         boolean browsing = true;
         while (browsing) {
             System.out.println("MENU BROWSING OPTIONS:");
             System.out.println("1. View All Items");
             System.out.println("2. Filter by Type");
             System.out.println("3. Filter by Price Range");
             System.out.println("4. Sort by Price (Low to High)");
             System.out.println("5. Sort by Price (High to Low)");
             System.out.println("6. Go Back");
 
             switch (readChoice()) {
                case 1:
                    esql.executeQueryAndPrintResult("SELECT itemName, typeOfItem, price FROM Items ORDER BY typeOfItem, price;");

                    break;
                case 2:
                    // Filter by type
                    System.out.print("Enter type (e.g., drinks, sides, entree): ");
                    String type = in.readLine().trim();
                    String queryType = String.format("SELECT itemName, price FROM Items WHERE TRIM(typeOfItem) = '%s';", type);
                    esql.executeQueryAndPrintResult(queryType);

                    break;
                case 3:
                    System.out.print("Enter minimum price: ");
                    double minPrice = Double.parseDouble(in.readLine());
                    System.out.print("Enter maximum price: ");
                    double maxPrice = Double.parseDouble(in.readLine());
                    String queryPrice = String.format(
                         "SELECT itemName, price FROM Items WHERE price BETWEEN %f AND %f;", minPrice, maxPrice);
                    esql.executeQueryAndPrintResult(queryPrice);
                    break;
                case 4:
                    esql.executeQueryAndPrintResult("SELECT itemName, price FROM Items ORDER BY price ASC;");
                    break;
                case 5:
                    esql.executeQueryAndPrintResult("SELECT itemName, price FROM Items ORDER BY price DESC;");
                    break;
                 case 6:
                    browsing = false; // Exit the menu browsing loop
                    break;
                 default:
                    System.out.println("Invalid choice. Try again.");
                    break;
             }
        }
     } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
   }
   public static void placeOrder(PizzaStore esql, String authorisedUser) {
      try {
         System.out.println("Available Stores:");
         esql.executeQueryAndPrintResult("SELECT storeID, address FROM Store;");
         System.out.print("Enter Store ID to place your order: ");
         int storeID = Integer.parseInt(in.readLine().trim());
 
         List<String> itemNames = new ArrayList<>();
         List<Integer> quantities = new ArrayList<>();
         boolean addingItems = true;
         double totalPrice = 0.0;
 
         while (addingItems) {
            System.out.print("Enter item name (or type 'done' to finish): ");
            String itemName = in.readLine().trim();
            if (itemName.equalsIgnoreCase("done")) break;
 
            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(in.readLine().trim());
 
             // Fetch price of the item
            String priceQuery = String.format("SELECT price FROM Items WHERE LOWER(itemName) = LOWER('%s');", itemName.replace("'", "''"));
            List<List<String>> priceResult = esql.executeQueryAndReturnResult(priceQuery);
 
            if (priceResult.isEmpty()) {
               System.out.println("Invalid item name. Please try again.");
               continue;
            }
 
            double price = Double.parseDouble(priceResult.get(0).get(0));
            totalPrice += price * quantity;
 
            int index = itemNames.indexOf(itemName);
            if (index != -1) {
               // Item already exists in the list, update the quantity
               quantities.set(index, quantities.get(index) + quantity);
            } else {
               // Item is new, add it to the list
               itemNames.add(itemName);
               quantities.add(quantity);
            }

         }
 
         if (itemNames.isEmpty()) {
             System.out.println("Order canceled. No items were added.");
             return;
         }
         // Insert new order
         String insertOrderQuery = String.format(
         "INSERT INTO FoodOrder (orderID, login, storeID, totalPrice, orderTimestamp, orderStatus) " +
         "VALUES (NEXTVAL('foodorder_orderid_seq'), '%s', %d, %.2f, NOW(), 'Order Received') RETURNING orderID;", 
         authorisedUser, storeID, totalPrice);
 
         List<List<String>> orderResult = esql.executeQueryAndReturnResult(insertOrderQuery);
         int orderID = Integer.parseInt(orderResult.get(0).get(0));
 
         for (int i = 0; i < itemNames.size(); i++) {
            // Fetch the correctly formatted item name from the Items table
            String correctedItemNameQuery = String.format(
               "SELECT itemName FROM Items WHERE LOWER(itemName) = LOWER('%s');",
               itemNames.get(i).replace("'", "''"));

            List<List<String>> correctedItemNameResult = esql.executeQueryAndReturnResult(correctedItemNameQuery);

            if (correctedItemNameResult.isEmpty()) {
               System.out.println("Error: Item '" + itemNames.get(i) + "' does not exist in the database.");
               return; // Stop the order if an invalid item is found
            }
            // Add to itemsinorder.csv
            String correctedItemName = correctedItemNameResult.get(0).get(0);
            String insertItemQuery = String.format(
                "INSERT INTO ItemsInOrder (orderID, itemName, quantity) VALUES (%d, '%s', %d) " +
                "ON CONFLICT (orderID, itemName) DO UPDATE " +
                "SET quantity = ItemsInOrder.quantity + EXCLUDED.quantity;",
                orderID, correctedItemName.replace("'", "''"), quantities.get(i));
            esql.executeUpdate(insertItemQuery);
        }
 
        System.out.println("Order placed successfully! Total price: $" + totalPrice);
    } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
    }
   }
   public static void viewAllOrders(PizzaStore esql, String authorisedUser) {
    try {
        String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s';", authorisedUser);
        List<List<String>> result = esql.executeQueryAndReturnResult(roleCheckQuery);
 
        if (result.isEmpty()) {
            System.out.println("Error retrieving user role.");
            return;
        }
 
        String userRole = result.get(0).get(0).trim().toLowerCase();
 
        String orderQuery;
        if (userRole.equals("manager") || userRole.equals("driver")) {
            // Managers and drivers can see all orders
            System.out.println("Displaying all customer orders:");
            orderQuery = "SELECT orderID, login, storeID, totalPrice, orderStatus FROM FoodOrder ORDER BY orderID DESC;";
        } else {
            // Customers can only see their own order history
            System.out.println("Displaying your order history:");
            orderQuery = String.format(
                "SELECT orderID, storeID, totalPrice, orderStatus FROM FoodOrder WHERE login = '%s' ORDER BY orderID DESC;",
                authorisedUser);
        }
 
        esql.executeQueryAndPrintResult(orderQuery);
 
    } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
    }
   }
   public static void viewRecentOrders(PizzaStore esql, String authorisedUser) {
      try {
        String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s';", authorisedUser);
        List<List<String>> result = esql.executeQueryAndReturnResult(roleCheckQuery);
 
        if (result.isEmpty()) {
            System.out.println("Error retrieving user role.");
            return;
        }
 
        String userRole = result.get(0).get(0).trim().toLowerCase();
 
        String orderQuery;
        if (userRole.equals("manager") || userRole.equals("driver")) {
            // Managers and drivers can see the 5 most recent orders from everyone
            System.out.println("Displaying the 5 most recent customer orders:");
            orderQuery = "SELECT orderID, login, storeID, totalPrice, orderStatus FROM FoodOrder ORDER BY orderID DESC LIMIT 5;";
        } else {
            // Customers can only see their own 5 most recent orders
            System.out.println("Displaying your 5 most recent orders:");
            orderQuery = String.format(
                "SELECT orderID, storeID, totalPrice, orderStatus FROM FoodOrder WHERE login = '%s' ORDER BY orderID DESC LIMIT 5;",
                authorisedUser);
        }
 
        esql.executeQueryAndPrintResult(orderQuery);
 
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
   }
   public static void viewOrderInfo(PizzaStore esql, String authorisedUser) {
      try {
        String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s';", authorisedUser);
        List<List<String>> result = esql.executeQueryAndReturnResult(roleCheckQuery);
 
        if (result.isEmpty()) {
            System.out.println("Error retrieving user role.");
            return;
        }
 
        String userRole = result.get(0).get(0).trim().toLowerCase();

        System.out.print("Enter the Order ID to view details: ");
        int orderID = Integer.parseInt(in.readLine().trim());

        // Check if user can access order info
        String orderCheckQuery = String.format(
            "SELECT orderID, login, orderTimestamp, totalPrice, orderStatus FROM FoodOrder WHERE orderID = %d;",
            orderID);
        List<List<String>> orderResult = esql.executeQueryAndReturnResult(orderCheckQuery);
 
        if (orderResult.isEmpty()) {
            System.out.println("Order not found.");
            return;
        }
 
        String orderOwner = orderResult.get(0).get(1).trim();

        if (!userRole.equals("manager") && !userRole.equals("driver") && !orderOwner.equals(authorisedUser)) {
            System.out.println("Permission denied. You can only view your own orders.");
            return;
        }

        System.out.println("Order Details:");
        System.out.println("Order ID: " + orderResult.get(0).get(0));
        System.out.println("Customer: " + orderOwner);
        System.out.println("Timestamp: " + orderResult.get(0).get(2));
        System.out.println("Total Price: $" + orderResult.get(0).get(3));
        System.out.println("Status: " + orderResult.get(0).get(4));

        System.out.println("\nItems in this order:");
        String itemsQuery = String.format(
            "SELECT itemName, quantity FROM ItemsInOrder WHERE orderID = %d;",
            orderID);
        List<List<String>> itemsResult = esql.executeQueryAndReturnResult(itemsQuery);

        if (itemsResult.isEmpty()) {
            System.out.println("No items found for this order.");
        } else {
            System.out.printf("%-25s %-10s\n", "Item Name", "Quantity");
            System.out.println("--------------------------------------");
            for (List<String> row : itemsResult) {
                System.out.printf("%-25s %-10s\n", row.get(0), row.get(1));
            }
        }

    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
   }
   public static void viewStores(PizzaStore esql) {
      try {
        System.out.println("Available Stores:");
         
        // Query to fetch all store information
        String storeQuery = "SELECT storeID, address, city, state, isOpen, reviewScore FROM Store ORDER BY storeID;";
         
        esql.executeQueryAndPrintResult(storeQuery);
 
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
   }
   public static void updateOrderStatus(PizzaStore esql, String authorisedUser) {
      try {
        String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s';", authorisedUser);
        List<List<String>> result = esql.executeQueryAndReturnResult(roleCheckQuery);
 
        if (result.isEmpty()) {
            System.out.println("Error retrieving user role.");
            return;
        }
 
        String userRole = result.get(0).get(0).trim().toLowerCase();
        if (!userRole.equals("driver") && !userRole.equals("manager")) {
            System.out.println("Permission denied. Only drivers and managers can update order status.");
            return;
        }
 
        System.out.println("Available Orders:");
        esql.executeQueryAndPrintResult("SELECT orderID, login, storeID, totalPrice, orderStatus FROM FoodOrder;");
         
        System.out.print("Enter the Order ID to update: ");
        int orderID = Integer.parseInt(in.readLine().trim());
 
        String orderCheckQuery = String.format("SELECT * FROM FoodOrder WHERE orderID = %d;", orderID);
        List<List<String>> orderResult = esql.executeQueryAndReturnResult(orderCheckQuery);
 
        if (orderResult.isEmpty()) {
            System.out.println("Order not found.");
            return;
        }
 
        System.out.println("Available Status Options:");
        System.out.println("1. Order Received");
        System.out.println("2. Preparing");
        System.out.println("3. Out for Delivery");
        System.out.println("4. Delivered");
        System.out.print("Choose a new status: ");
        int statusChoice = Integer.parseInt(in.readLine().trim());
 
        String newStatus = "";
        switch (statusChoice) {
            case 1: newStatus = "Order Received"; break;
            case 2: newStatus = "Preparing"; break;
            case 3: newStatus = "Out for Delivery"; break;
            case 4: newStatus = "Delivered"; break;
            default:
                System.out.println("Invalid status choice. Please try again.");
                return;
        }
        
        String updateQuery = String.format(
            "UPDATE FoodOrder SET orderStatus = '%s' WHERE orderID = %d;",
            newStatus, orderID);
        esql.executeUpdate(updateQuery);
        System.out.println("Order status updated successfully!");
 
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
   }
   public static void updateMenu(PizzaStore esql, String authorisedUser) {
      try {
        String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s';", authorisedUser);
        List<List<String>> result = esql.executeQueryAndReturnResult(roleCheckQuery);
 
        if (result.isEmpty() || !result.get(0).get(0).trim().equalsIgnoreCase("manager")) {
            System.out.println("Permission denied. Only managers can update the menu.");
            return;
        }

        System.out.print("Enter the name of the food item to update (or type new product name to add a new item): ");
        String itemName = in.readLine().trim();
 
        String itemCheckQuery = String.format("SELECT * FROM Items WHERE LOWER(itemName) = LOWER('%s');", itemName);
        List<List<String>> itemResult = esql.executeQueryAndReturnResult(itemCheckQuery);
 
        if (itemResult.isEmpty()) {
            System.out.println("Item not found. Would you like to add this item? (yes/no)");
            String response = in.readLine().trim().toLowerCase();
            if (response.equals("yes")) {
                addNewItem(esql, itemName);
            } else {
             System.out.println("Update canceled.");
            }
            return;
        }

        boolean updating = true;
        while (updating) {
            System.out.println("UPDATE ITEM OPTIONS:");
            System.out.println("1. Update Price");
            System.out.println("2. Update Type");
            System.out.println("3. Update Ingredients");
            System.out.println("4. Update Description");
            System.out.println("5. Delete Item");
            System.out.println("6. Go Back");
         
            switch (readChoice()) {
               case 1:
                  System.out.print("Enter new price: ");
                  double newPrice = Double.parseDouble(in.readLine().trim());
                  String updatePriceQuery = String.format(
                     "UPDATE Items SET price = %.2f WHERE LOWER(itemName) = LOWER('%s');",
                     newPrice, itemName.replace("'", "''"));
                  esql.executeUpdate(updatePriceQuery);
                  System.out.println("Price updated successfully!");
                  break;
               case 2:
                  System.out.print("Enter new type (e.g., drinks, sides, entree): ");
                  String newType = in.readLine().trim();
                   String updateTypeQuery = String.format(
                     "UPDATE Items SET typeOfItem = '%s' WHERE LOWER(itemName) = LOWER('%s');",
                     newType.replace("'", "''"), itemName.replace("'", "''"));
                  esql.executeUpdate(updateTypeQuery);
                  System.out.println("Type updated successfully!");
                  break;
               case 3:
                  System.out.print("Enter new ingredients: ");
                  String newIngredients = in.readLine().trim();
                  String updateIngredientsQuery = String.format(
                     "UPDATE Items SET ingredients = '%s' WHERE LOWER(itemName) = LOWER('%s');",
                     newIngredients.replace("'", "''"), itemName.replace("'", "''"));
                  esql.executeUpdate(updateIngredientsQuery);
                  System.out.println("Ingredients updated successfully!");
                  break;
               case 4:
                  System.out.print("Enter new description: ");
                  String newDescription = in.readLine().trim();
                  String updateDescriptionQuery = String.format(
                     "UPDATE Items SET description = '%s' WHERE LOWER(itemName) = LOWER('%s');",
                     newDescription.replace("'", "''"), itemName.replace("'", "''"));
                  esql.executeUpdate(updateDescriptionQuery);
                  System.out.println("Description updated successfully!");
                  break;
               case 5:
                  deleteItem(esql, itemName);
                  updating = false; // Exit update menu after deletion
                  break;
               case 6:
                  updating = false;
                  break;
               default:
                  System.out.println("Invalid choice. Try again.");
                  break;
            }
         }         
     } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
   }

   public static void addNewItem(PizzaStore esql, String itemName) {
      try {
        System.out.print("Enter type (e.g., drinks, sides, entree): ");
        String type = in.readLine().trim();
  
        System.out.print("Enter price: ");
        double price = Double.parseDouble(in.readLine().trim());
  
        System.out.print("Enter ingredients: ");
        String ingredients = in.readLine().trim();
  
        System.out.print("Enter description: ");
        String description = in.readLine().trim();
  
        // Insert the new item into the database
        String insertItemQuery = String.format(
            "INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) VALUES ('%s', '%s', '%s', %.2f, '%s');",
            itemName.replace("'", "''"), ingredients.replace("'", "''"), type.replace("'", "''"), price, description.replace("'", "''"));
  
        esql.executeUpdate(insertItemQuery);
        System.out.println("New item added successfully!");
  
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
  }

  public static void deleteItem(PizzaStore esql, String itemName) {
      try {
     // Check if item exists in ItemsInOrder (i.e., has been ordered before)
        String checkOrderQuery = String.format(
            "SELECT COUNT(*) FROM ItemsInOrder WHERE LOWER(itemName) = LOWER('%s');",
            itemName.replace("'", "''"));

        List<List<String>> orderResult = esql.executeQueryAndReturnResult(checkOrderQuery);
        int orderCount = Integer.parseInt(orderResult.get(0).get(0));

        if (orderCount > 0) {
            System.out.println("Cannot delete item. It is associated with existing orders.");
            return;
        }

         String deleteQuery = String.format(
            "DELETE FROM Items WHERE LOWER(itemName) = LOWER('%s');",
            itemName.replace("'", "''"));

         esql.executeUpdate(deleteQuery);
         System.out.println("Item successfully deleted from the menu.");

      } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
      }
   }

   public static void updateUser(PizzaStore esql, String authorisedUser) {
      try {
         String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s';", authorisedUser);
         List<List<String>> result = esql.executeQueryAndReturnResult(roleCheckQuery);
 
         if (result.isEmpty() || !result.get(0).get(0).trim().equalsIgnoreCase("manager")) {
             System.out.println("Permission denied. Only managers can update user roles.");
             return;
         }
 
         System.out.print("Enter the login of the user to update: ");
         String userToUpdate = in.readLine().trim();
 
         String checkUserQuery = String.format("SELECT * FROM Users WHERE login = '%s';", userToUpdate);
        List<List<String>> userResult = esql.executeQueryAndReturnResult(checkUserQuery);

        if (userResult.isEmpty()) {
         System.out.println("User not found.");
         return;
        }

        // Menu for updating user details
        boolean updating = true;
        while (updating) {
            System.out.println("UPDATE USER OPTIONS:");
            System.out.println("1. Change Phone Number");
            System.out.println("2. Change Favorite Item");
            System.out.println("3. Change Password");
            System.out.println("4. Change Role");
            System.out.println("5. Go Back");

            switch (readChoice()) {
                case 1:
                    System.out.print("Enter new phone number: ");
                    String newPhone = in.readLine().trim();
                    String updatePhoneQuery = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s';", newPhone, userToUpdate);
                    esql.executeUpdate(updatePhoneQuery);
                    System.out.println("Phone number updated successfully!");
                    break;
                case 2:
                    System.out.print("Enter new favorite item: ");
                    String newFav = in.readLine().trim();
                    String updateFavQuery = String.format("UPDATE Users SET favoriteItems = '%s' WHERE login = '%s';", newFav.replace("'", "''"), userToUpdate);
                    esql.executeUpdate(updateFavQuery);
                    System.out.println("Favorite item updated successfully!");
                    break;
                case 3:
                    System.out.print("Enter new password: ");
                    String newPass = in.readLine().trim();
                    String updatePassQuery = String.format("UPDATE Users SET password = '%s' WHERE login = '%s';", newPass, userToUpdate);
                    esql.executeUpdate(updatePassQuery);
                    System.out.println("Password updated successfully!");
                    break;
                case 4:
                    System.out.print("Enter new role (customer/driver/manager): ");
                    String newRole = in.readLine().trim();
                    if (!newRole.equalsIgnoreCase("customer") && !newRole.equalsIgnoreCase("driver") && !newRole.equalsIgnoreCase("manager")) {
                        System.out.println("Invalid role. Please enter 'customer', 'driver', or 'manager'.");
                        break;
                    }
                    String updateRoleQuery = String.format("UPDATE Users SET role = '%s' WHERE login = '%s';", newRole, userToUpdate);
                    esql.executeUpdate(updateRoleQuery);
                    System.out.println("User role updated successfully!");
                    break;
                case 5:
                    updating = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
                    break;
            }
        }
     } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
     }
   }


}//end PizzaStore

