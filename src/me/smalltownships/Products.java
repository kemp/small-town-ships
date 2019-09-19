package me.smalltownships;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of products
 * 
 * @author kemp
 */
public class Products {

	/**
	 * Fetch a list of all products
	 * 
	 * @return the products
	 */
	public static List<Product> getProducts() {
    	List<Product> productsList = new ArrayList<Product>();
    	
    	// TODO @kemp: list of products is hard-coded
    	
    	productsList.add(new Product(
			1, 
			"Iowa Class", 
			"<p>Iowa class battleships are the culmination of almost 100 years of battleship development. Their speed of 33 knots puts them at the top end of battleships in speed and allows them to provide support for fast carrier groups. The Iowa class battleships also provide excellent capital ships with their high conning towers and large anti-air capability.</p>\n", 
			"<ul>\n" + 
			"<li>Speed: 33 knots</li>\n" + 
			"<li>Dimensions:<ul>\n" + 
			"<li>Length: 270 meters</li>\n" + 
			"<li>Beam: 33 meters</li>\n" + 
			"<li>Draught: 11 meters</li>\n" + 
			"</ul>\n" + 
			"</li>\n" + 
			"<li>Armament:<ul>\n" + 
			"<li>Main: 9x405mm guns distributed evenly in two fore turrets and one aft turret.</li>\n" + 
			"<li>Secondary: 20x125mm guns, 80x40mm guns, and 50x20mm guns.</li>\n" + 
			"</ul>\n" + 
			"</li>\n" + 
			"</ul>\n" + 
			"", 
			1820000000,
			3,
			"https://nationalinterest.org/sites/default/files/main_images/DN-ST-94-00424_(17258233731).jpg"
		));
    	
    	productsList.add(new Product(
			2, 
			"Deutschland Class", 
			"<p>While the Deutschland class “pocket” battleships do not have the firepower to match cannons with most other battleships they pack enough firepower to best any cruiser. Deutschland class battleships also have excellent speed which makes them idea commerce raiders. The lower price tag on these battleships and their overall versatility makes them excellent additions to any financially conscious battle fleet.</p>\n", 
			"<ul>\n" + 
			"<li>Speed: 28 knots</li>\n" + 
			"<li>Dimensions:<ul>\n" + 
			"<li>Length: 186 meters</li>\n" + 
			"<li>Beam: 21.6 meters</li>\n" + 
			"<li>Draught: 5.8 meters</li>\n" + 
			"</ul>\n" + 
			"</li>\n" + 
			"<li>Armament:<ul>\n" + 
			"<li>Main: 6x280mm guns distributed evenly in one fore turret and one aft turret.</li>\n" + 
			"<li>Secondary: 8x150mm guns, 6x105mm guns, 6x20mm guns.</li>\n" + 
			"<li>Torpedoes: 8x535mm</li>\n" + 
			"</ul>\n" + 
			"</li>\n" + 
			"</ul>\n" + 
			"", 
			1550000000,
			7,
			"https://upload.wikimedia.org/wikipedia/commons/8/8e/Bundesarchiv_DVM_10_Bild-23-61-51%2C_Geschwader_in_Kiellinie.jpg"
		));
    	
    	productsList.add(new Product(
			3, 
			"Bismark Class", 
			"<p>Bismarck class battleships are excellent for use as heavy battleships or as capital ships. Their high speed also allows them to outmaneuver slower battleships and protect fast carrier groups. Bismarck class battleships do lack heavy anti-air defenses and so are best paired with other ships with heavier anti-air defenses.</p>\n", 
			"<ul>\n" + 
			"<li>Speed: 29 knots</li>\n" + 
			"<li>Dimensions:<ul>\n" + 
			"<li>Length: 248 meters</li>\n" + 
			"<li>Beam: 36 meters</li>\n" + 
			"<li>Draught: 8.5 meters</li>\n" + 
			"</ul>\n" + 
			"</li>\n" + 
			"<li>Armament:<ul>\n" + 
			"<li>Main: 8x380mm guns distributed evenly in two fore turrets and two aft turrets.</li>\n" + 
			"<li>Secondary: 12x150mm guns, 16x105mm guns, 16x37mm guns, 12x20mm guns.</li>\n" + 
			"</ul>\n" + 
			"</li>\n" + 
			"</ul>\n" + 
			"", 
			1699000000,
			20, 
			"https://img00.deviantart.net/34d7/i/2012/256/9/f/battleship_bismarck_by_rainerkalwitz-d5ejpqd.jpg"
		));
    	
    	return productsList;
	}
	
	public static Product findProductByID(int id) {
		for (Product p : getProducts()) {
			if (p.getId() == id) {
				return p;
			}
		}
		
		return null;
	}
}
