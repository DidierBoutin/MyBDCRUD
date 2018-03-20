
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
 

// Classe d'exécution de l'application BDBA
public class MyFood {
	
	
	private static Scanner clavier = new Scanner(System.in);

	// List of food attributes
	private static final String[] FOOD_ATTRIBUTES = { "Food's Name", "Category", "Energy (kcal)" };
	// List of food attributes
	private static final String[] CATEGORY_ATTRIBUTES = { "Category's Name", "Category's Wording" };
	 
	
	
	public static void main(String[] args) {

		// try whith ressource connexion to base (it will be closed automatically)
		try (Connection connexion= DriverManager.getConnection("jdbc:postgresql://stampy.db.elephantsql.com:5432", "wxerlxwe", "iDbFBKA6DYR9bdwrwUGviSMl1OlZ5E-E"))

		{  
            // Loop on main menu
			String menuChoice = "";
			do {
				// Display menu in console
				System.out.println();
				System.out.println("Hello,  welcome in BDBA");
				System.out.println("1) Add a food");
				System.out.println("2) Delete a food");
				System.out.println("3) Dispaly all foods");
				System.out.println("4) Create a new category");
				System.out.println("0) Exit");

				try {
					// Get menu choice from console
					menuChoice = clavier.nextLine();
					switch (menuChoice) {
					case "1":
						addFood(connexion);
						break;
					case "2":
						deleteFood(connexion);
						break;
					case "3":
						displayTableFoodAll(connexion) ;
						break;
					case "4":
						addCategory(connexion) ;
						break;
					case "0":
						System.out.println("bye bye!");
						break;
					default:
						System.out.println("Commande inconnue ! Enlevez vos moufles svp");
						break;
					}
				// if pb on the console, display error message and exit
				} catch (IOException e) {
					System.out.println("Un problème technique survenu : " + e);
					System.out.println("Fermeture du programme.");
					menuChoice = "0";
				}

				// while menu choice is not "0" we keep printing the menu
			} while (!menuChoice.equals("0"));

			// don't forget close Scanner!!!
			clavier.close();
		}
		// if pb at connexion base, display error message whitout exit. User can try again if concurence pb
		catch(SQLException e) {

			System.out.println("Connexion KO : " + e);
		}
	}

	 //******************create a new Food*********************************
	
	private static void addFood(Connection c) throws  SQLException {

		// Loop on all messages, save values in  String array 
		String []  saveFood = new String [FOOD_ATTRIBUTES.length];
		for (int i=0; i != FOOD_ATTRIBUTES.length; ++i) {
			saveFood[i]= getFoodAttributeFromConsole(FOOD_ATTRIBUTES[i]);
		}
	
		 
		// test if calorie is numeric
		try {
			Integer cal = Integer.parseInt(saveFood[2]); 
			Food f = new Food(saveFood[0], saveFood[1], cal);
			
			//test if category exists
			if (categorieExisits(f.getCategorie(), c))   
				{ if (insertToFood(f,c)) {
					f.printFood();System.out.println("is save in table Food");}
				else {} 
				}
			else {System.out.println("category must exists ! Try again");}
			}
		
        // display message if calorie is not numeric
		catch (NumberFormatException e)
		{System.out.println("energy must be numéric, try again");}
	}
	
	
	 //******************create a new category*********************************
	private static void addCategory(Connection c) throws  SQLException {

		// Loop on all attribute categorie
		String []  saveCat = new String [CATEGORY_ATTRIBUTES.length];
		for (int i=0; i != CATEGORY_ATTRIBUTES.length; ++i) {
			saveCat[i]= getFoodAttributeFromConsole(CATEGORY_ATTRIBUTES[i]);
		}
		
		try { 
			 if (insertToCategory(saveCat[0],saveCat[1], c)) {
				System.out.println("New category created");}		
		}
		catch (SQLException e )
		{System.out.println("error SQL, try again " + e);}
	}

	
	 //****************** delete a food *********************************
	
	private static void deleteFood(Connection c) throws IOException, SQLException {

		String food= getFoodAttributeFromConsole(FOOD_ATTRIBUTES[0]);
		
		List <Food> fdel = deleteFromTableFood(food,c);
		
		if (fdel == null)
			{System.out.println("There is no " + fdel + "in table Food");;}
		else
			{System.out.println("Foods deleted :");
			fdel.forEach(item-> {item.printFood();});
			}		 
	}
	 
	
	 //****************** get keyboard input *********************************

	private static String getFoodAttributeFromConsole(String message) {
		String foodAttribute = "";
		System.out.println(message);
		foodAttribute = clavier.nextLine();
		return foodAttribute;
	}


	 //****************** verify category exists *********************************
	private static boolean categorieExisits(String categorie, Connection c) throws SQLException{ 
		try{
			Statement statement = c.createStatement();
			String query = "SELECT * from categorie where id = '" + categorie + "';" ;
			ResultSet resultat = statement.executeQuery(query );
			return (resultat.next() );  
		}
		catch(SQLException e) {
			System.out.println("this category doesn't exists  : " + "try again");
			return false;
		}
	}
	
	
	 //****************** delete in Table Food *********************************
	private static List<Food>  deleteFromTableFood(String f, Connection c) throws SQLException{ 
		try {
			List<Food> selFood = new ArrayList<>();	
			selFood = selectTableFoodByName( f, c);
			if (selFood == null) {
				return null ;
			  }		
			else {	
				String query = "delete from food  where name =  '" + f + "';";
				Statement state = c.createStatement();	 
				state.executeUpdate(query);
				return selFood ;
			}}
		catch(SQLException e) {
			System.out.println("error SQL, try again : " + e);
            return null;
		}
	}
	
	 //****************** insert in Table Food *********************************
	private static boolean insertToFood(Food f, Connection c) throws SQLException{ 
		try {

			String query = "INSERT INTO food (name, id_categorie, energie ) values ('"+f.getName()+"', '"+f.getCategorie()+"',"+ f.getEnergie()+")";
			Statement state = c.createStatement();
			
			// On met en parmrtre Statement.RETURN_GENERATED_KEYS qu'on puisse récupérer les clés automatiquement générée
			state.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			
			//getGeneratedKeys get  all generated lignes  in statement state, key is first parameter
			ResultSet keys = state.getGeneratedKeys(); 
		     
			// At the first get ligne  , get colonne 1 which contains the key automacaly created
		    Long lastKey = keys.next() ? keys.getLong(1) : 0;
 
		    //update class Food with  id
		    f.setId(lastKey);  
           
            return true;   
		}

		catch(SQLException e) {
			System.out.println("error, try again");
            return false;
		}

	}
	
	 //****************** insert in Table Category*********************************

	private static boolean insertToCategory(String name, String wording, Connection c ) throws SQLException{ 
		try {
			String query = "INSERT INTO categorie (id, libelle ) values ('" + name + "', '" + wording + "')";
			System.out.println(query);
			Statement state = c.createStatement();
			
			state.executeUpdate(query);
			
            return true; 
		}
		catch(SQLException e) {
			System.out.println("category exists already, or sql error, try again " + e);
            return false;
		}
	}
	
	 //****************** get a food in Table Food by its name ***************************
	public static List<Food> selectTableFoodByName(String food, Connection c) {
		try{
			List<Food> SelFood = new ArrayList<>();	
			Statement statement = c.createStatement();
			String query = "SELECT * from food where name = '" + food + "' order by id_categorie, name;";
			 
			ResultSet resultat = statement.executeQuery( query);
			
			if (!resultat.next() )
				{return null;}
			else
				{int id = resultat.getInt( "id" );
				String name = resultat.getString( "name" );
				String categorie = resultat.getString("id_categorie");
				int energie = resultat.getInt( "energie" );
				SelFood.add(new Food(id, name, categorie,energie));
				while ( resultat.next() ) {
					id = resultat.getInt( "id" );
					name = resultat.getString( "name" );
				    categorie = resultat.getString("id_categorie");
					energie = resultat.getInt( "energie" );
					SelFood.add(new Food(id, name, categorie,energie));
			}
			}
			return SelFood;
			}
		catch(SQLException e) {
			System.out.println("Erro sql on select: " + e);return null;
		}
	}
	
	
	 //****************** list all table Food ***************************

	public static void displayTableFoodAll(Connection c) {
		try{
			 
			Statement statement = c.createStatement();
			String query = "SELECT * from food order by id_categorie, name;";
			 
			ResultSet resultat = statement.executeQuery( query);
			
			if (!resultat.next() )
				{System.out.println("Table food is empty !!!");}
			else
				{
				Food fd = new Food(resultat.getInt( "id" ), resultat.getString( "name" ), resultat.getString("id_categorie"), resultat.getInt( "energie" )); 
				fd.printFoodByLine(); 
				while ( resultat.next() ) {
					fd = new Food(resultat.getInt( "id" ), resultat.getString( "name" ), resultat.getString("id_categorie"), resultat.getInt( "energie" )); 
					fd.printFoodByLine(); 
				}
			}
		 
			}
		catch(SQLException e) {
			System.out.println("Erro sql on select: " + e);
		}
	}
}

