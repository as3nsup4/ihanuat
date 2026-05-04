package com.ihanuat.mod.modules.profitTracker;

import java.util.Map;
import java.util.Set;

/**
 * Static constants for tracked items, bazaar mappings, and category sets
 * used throughout the profit tracking system.
 */
public final class ItemConstants {

    private ItemConstants() {}

    public static final long MAX_CULTIVATING_DELTA = 10_000L;

    public static final Set<String> CROPS_SET = Set.of(
            "Wheat", "Enchanted Wheat", "Enchanted Hay Bale",
            "Seeds", "Enchanted Seeds", "Box of Seeds",
            "Potato", "Enchanted Potato", "Enchanted Baked Potato",
            "Carrot", "Enchanted Carrot", "Enchanted Golden Carrot",
            "Melon Slice", "Melon Block", "Enchanted Melon Slice", "Enchanted Melon",
            "Pumpkin", "Enchanted Pumpkin", "Polished Pumpkin",
            "Sugar Cane", "Enchanted Sugar", "Enchanted Sugar Cane",
            "Cactus", "Enchanted Cactus Green", "Enchanted Cactus",
            "Red Mushroom", "Enchanted Red Mushroom", "Enchanted Red Mushroom Block",
            "Brown Mushroom", "Enchanted Brown Mushroom", "Enchanted Brown Mushroom Block",
            "Cocoa Beans", "Enchanted Cocoa Beans", "Enchanted Cookie",
            "Nether Wart", "Enchanted Nether Wart", "Mutant Nether Wart",
            "Sunflower", "Enchanted Sunflower", "Compacted Sunflower",
            "Moonflower", "Enchanted Moonflower", "Compacted Moonflower",
            "Wild Rose", "Enchanted Wild Rose", "Compacted Wild Rose");

    public static final Set<String> PEST_ITEMS_SET = Set.of(
            "Beady Eyes", "Chirping Stereo", "Sunder VI Book", "Clipped Wings",
            "Bookworm's Favorite Book", "Atmospheric Filter", "Wriggling Larva",
            "Pesterminator I Book", "Squeaky Toy", "Squeaky Mousemat",
            "Fire in a Bottle", "Vermin Vaporizer Chip", "Mantid Claw",
            "Overclocker 3000", "Vinyl",
            "Dung", "Honey Jar", "Plant Matter", "Tasty Cheese", "Compost", "Jelly",
            "Pest Shard", "Locust Larva");

    public static final Set<String> PETS_SET = Set.of("Epic Slug", "Legendary Slug", "Rat");

    public static final Set<String> MISC_DROPS_SET = Set.of(
            "Cropie", "Squash", "Fermento", "Helianthus",
            "Ashwreath", "Choconut", "Dustgrain", "Gloomgourd", "Lonelily",
            "Scourroot", "Shadevine", "Veilshroom", "Witherbloom", "Chocoberry",
            "Cindershade", "Coalroot", "Creambloom", "Duskbloom", "Thornshade",
            "Blastberry", "Cheesebite", "Chloronite", "Do-not-eat-shroom", "Fleshtrap",
            "Magic Jellybean", "Noctilume", "Snoozling", "Soggybud", "Chorus Fruit",
            "PlantBoy Advance", "Puffercloud", "Shellfruit", "Startlevine", "Stoplight Petal",
            "Thunderling", "Turtlellini", "Zombud", "All-in Aloe", "Devourer",
            "Glasscorn", "Godseed", "Jerryflower", "Phantomleaf", "Timestalk",
            "Tool EXP Capsule", "Pet XP", "Purse");

    /**
     * Harvest Feast event drops (SkyBlock 0.24.4 — Nov 2026). These are the
     * 1/18,000 "RARE CROP!" crafting materials that drop while harvesting
     * in-season crops during the Harvest Feast event — one base material
     * per featured crop. Crafted derivatives (e.g. Cornucopia Crystal,
     * Turbo Gourd) are recipes, not drops, and so are not tracked here.
     */
    public static final Set<String> FEAST_ITEMS_SET = Set.of(
            "Cornucopia",            // Wheat
            "Carrot Zest",           // Carrot
            "Deepfries",             // Potato
            "Aggourdian",            // Pumpkin
            "Cane Knot",             // Sugar Cane
            "Melon Juice",           // Melon
            "Cactus Flower",         // Cactus
            "Designer Coffee Beans", // Cocoa Beans
            "Feastfungus",           // Mushroom
            "Botroot",               // Nether Wart
            "Salted Sunflower Seeds",// Sunflower
            "Crystalized Moonlight", // Moonflower
            "Floral Gelatin",        // Wild Rose
            "Seasoning");            // Universal feast drop (no bazaar value)



    public static final Set<String> BASE_CROPS = Set.of(
            "Wheat", "Potato", "Carrot", "Melon Slice", "Pumpkin",
            "Sugar Cane", "Cactus", "Nether Wart", "Cocoa Beans",
            "Red Mushroom", "Brown Mushroom",
            "Sunflower", "Moonflower", "Wild Rose", "Seeds");

    public static final Map<String, Double> TRACKED_ITEMS = Map.ofEntries(
            // Crops
            Map.entry("Wheat", 6.0), Map.entry("Enchanted Wheat", 960.0), Map.entry("Enchanted Hay Bale", 153600.0),
            Map.entry("Seeds", 3.0), Map.entry("Enchanted Seeds", 480.0), Map.entry("Box of Seeds", 76800.0),
            Map.entry("Potato", 3.0), Map.entry("Enchanted Potato", 480.0),
            Map.entry("Enchanted Baked Potato", 76800.0),
            Map.entry("Carrot", 3.0), Map.entry("Enchanted Carrot", 480.0),
            Map.entry("Enchanted Golden Carrot", 76800.0),
            Map.entry("Melon Slice", 2.0), Map.entry("Melon Block", 18.0), Map.entry("Enchanted Melon Slice", 320.0),
            Map.entry("Enchanted Melon", 51200.0),
            Map.entry("Pumpkin", 10.0), Map.entry("Enchanted Pumpkin", 1600.0), Map.entry("Polished Pumpkin", 256000.0),
            Map.entry("Sugar Cane", 4.0), Map.entry("Enchanted Sugar", 640.0),
            Map.entry("Enchanted Sugar Cane", 102400.0),
            Map.entry("Cactus", 4.0), Map.entry("Enchanted Cactus Green", 640.0),
            Map.entry("Enchanted Cactus", 102400.0),
            Map.entry("Mushroom", 10.0), Map.entry("Red Mushroom", 10.0), Map.entry("Brown Mushroom", 10.0),
            Map.entry("Enchanted Red Mushroom", 1600.0), Map.entry("Enchanted Brown Mushroom", 1600.0),
            Map.entry("Enchanted Red Mushroom Block", 256000.0), Map.entry("Enchanted Brown Mushroom Block", 256000.0),
            Map.entry("Cocoa Beans", 3.0), Map.entry("Enchanted Cocoa Beans", 480.0),
            Map.entry("Enchanted Cookie", 76800.0),
            Map.entry("Nether Wart", 4.0), Map.entry("Enchanted Nether Wart", 640.0),
            Map.entry("Mutant Nether Wart", 102400.0),
            Map.entry("Sunflower", 4.0), Map.entry("Enchanted Sunflower", 640.0),
            Map.entry("Compacted Sunflower", 102400.0),
            Map.entry("Moonflower", 4.0), Map.entry("Enchanted Moonflower", 640.0),
            Map.entry("Compacted Moonflower", 102400.0),
            Map.entry("Wild Rose", 4.0), Map.entry("Enchanted Wild Rose", 640.0),
            Map.entry("Compacted Wild Rose", 102400.0),
            // Pest Items
            Map.entry("Beady Eyes", 25000.0), Map.entry("Chirping Stereo", 100000.0), Map.entry("Sunder VI Book", 0.0),
            Map.entry("Clipped Wings", 25000.0), Map.entry("Bookworm's Favorite Book", 10000.0),
            Map.entry("Atmospheric Filter", 100000.0),
            Map.entry("Wriggling Larva", 250000.0), Map.entry("Pesterminator I Book", 0.0),
            Map.entry("Squeaky Toy", 10000.0),
            Map.entry("Squeaky Mousemat", 1000000.0), Map.entry("Fire in a Bottle", 100000.0),
            Map.entry("Vermin Vaporizer Chip", 0.0),
            Map.entry("Mantid Claw", 75000.0),
            Map.entry("Overclocker 3000", 250000.0),
            Map.entry("Vinyl", 50000.0),
            Map.entry("Dung", 0.0), Map.entry("Honey Jar", 0.0), Map.entry("Plant Matter", 0.0),
            Map.entry("Tasty Cheese", 0.0), Map.entry("Compost", 0.0), Map.entry("Jelly", 0.0),
            // Pets
            Map.entry("Epic Slug", 500000.0), Map.entry("Legendary Slug", 5000000.0), Map.entry("Rat", 5000.0),
            // Misc Drops
            Map.entry("Cropie", 25000.0), Map.entry("Squash", 75000.0), Map.entry("Fermento", 250000.0),
            Map.entry("Helianthus", 0.0), Map.entry("Tool EXP Capsule", 100000.0),

            Map.entry("Ashwreath", 0.0), Map.entry("Choconut", 0.0), Map.entry("Dustgrain", 0.0),
            Map.entry("Gloomgourd", 0.0), Map.entry("Lonelily", 0.0), Map.entry("Scourroot", 0.0),
            Map.entry("Shadevine", 0.0), Map.entry("Veilshroom", 0.0), Map.entry("Witherbloom", 0.0),
            Map.entry("Chocoberry", 0.0), Map.entry("Cindershade", 0.0), Map.entry("Coalroot", 0.0),
            Map.entry("Creambloom", 0.0), Map.entry("Duskbloom", 0.0), Map.entry("Thornshade", 0.0),
            Map.entry("Blastberry", 0.0), Map.entry("Cheesebite", 0.0), Map.entry("Chloronite", 0.0),
            Map.entry("Do-not-eat-shroom", 0.0), Map.entry("Fleshtrap", 0.0),
            Map.entry("Magic Jellybean", 0.0), Map.entry("Noctilume", 0.0), Map.entry("Snoozling", 0.0),
            Map.entry("Soggybud", 0.0), Map.entry("Chorus Fruit", 0.0),
            Map.entry("PlantBoy Advance", 0.0), Map.entry("Puffercloud", 0.0), Map.entry("Shellfruit", 0.0),
            Map.entry("Startlevine", 0.0), Map.entry("Stoplight Petal", 0.0), Map.entry("Thunderling", 0.0),
            Map.entry("Turtlellini", 0.0), Map.entry("Zombud", 0.0), Map.entry("All-in Aloe", 0.0),
            Map.entry("Devourer", 0.0), Map.entry("Glasscorn", 0.0), Map.entry("Godseed", 0.0),
            Map.entry("Jerryflower", 0.0), Map.entry("Phantomleaf", 0.0), Map.entry("Timestalk", 0.0),
            // Pet XP (price per XP point, derived from configured pet values)
            Map.entry("Pet XP", 0.0),
            Map.entry("Pest Shard", 0.0),
            // AH Items
            Map.entry("Biofuel", 0.0), Map.entry("Farming Exp Boost", 0.0),
            Map.entry("Harvest Harbinger V Potion", 0.0),
            Map.entry("Velvet Top Hat", 0.0), Map.entry("Cashmere Hat", 0.0), Map.entry("Satin Trousers", 0.0),
            Map.entry("Oxford Shoes", 0.0), Map.entry("Space Helmet", 0.0), Map.entry("Wild Strawberry Dye", 0.0),
            Map.entry("Copper Dye", 0.0), Map.entry("Clouds Rune I", 0.0), Map.entry("Fire Spiral Rune I", 0.0),
            Map.entry("Gem Rune I", 0.0), Map.entry("Golden Rune I", 0.0), Map.entry("Hearts Rune I", 0.0),
            Map.entry("Hot Rune I", 0.0), Map.entry("Lava Rune I", 0.0), Map.entry("Lightning Rune I", 0.0),
            Map.entry("Magical Rune I", 0.0), Map.entry("Music Rune I", 0.0), Map.entry("Rainbow Rune I", 0.0),
            Map.entry("Redstone Rune I", 0.0), Map.entry("Smokey Rune I", 0.0), Map.entry("Snow Rune I", 0.0),
            Map.entry("Sparkling Rune I", 0.0), Map.entry("Wake Rune I", 0.0), Map.entry("White Spiral Rune I", 0.0),
            Map.entry("Zap Rune I", 0.0),
            // Visitor / Rare Drops
            Map.entry("Overgrown Grass", 0.0), Map.entry("Flowering Bouqet", 0.0), Map.entry("Green Bandana", 0.0),
            Map.entry("Hypercharge Chip", 0.0), Map.entry("Quickdraw Chip", 0.0), Map.entry("Superboom TNT", 0.0),
            Map.entry("Green Candy", 0.0), Map.entry("Purple Candy", 0.0), Map.entry("Ice Essence", 0.0),
            Map.entry("Diamond Essence", 0.0), Map.entry("Gold Essence", 0.0), Map.entry("Jacob's Ticket", 0.0),
            Map.entry("Turbo-Cacti I Book", 0.0), Map.entry("Turbo-Cane I Book", 0.0),
            Map.entry("Turbo-Carrot I Book", 0.0),
            Map.entry("Turbo-Cocoa I Book", 0.0), Map.entry("Turbo-Melon I Book", 0.0),
            Map.entry("Turbo-Moonflower I Book", 0.0),
            Map.entry("Turbo-Mushrooms I Book", 0.0), Map.entry("Turbo-Potato I Book", 0.0),
            Map.entry("Turbo-Pumpkin I Book", 0.0),
            Map.entry("Turbo-Rose I Book", 0.0), Map.entry("Turbo-Sunflower I Book", 0.0),
            Map.entry("Turbo-Warts I Book", 0.0),
            Map.entry("Turbo-Wheat I Book", 0.0), Map.entry("Cultivating I Book", 0.0),
            Map.entry("Delicate V Book", 0.0),
            Map.entry("Replenish I Book", 0.0), Map.entry("Dedication IV Book", 0.0), Map.entry("Jungle Key", 0.0),
            Map.entry("Pet Cake", 0.0), Map.entry("Fine Flour", 0.0), Map.entry("Arachne Fragment", 0.0),
            Map.entry("Locust Larva", 0.0),
            // Feast (Harvest Feast base drops; prices populated live from bazaar)
            Map.entry("Cornucopia", 0.0), Map.entry("Carrot Zest", 0.0), Map.entry("Deepfries", 0.0),
            Map.entry("Aggourdian", 0.0), Map.entry("Cane Knot", 0.0), Map.entry("Melon Juice", 0.0),
            Map.entry("Cactus Flower", 0.0), Map.entry("Designer Coffee Beans", 0.0),
            Map.entry("Feastfungus", 0.0), Map.entry("Botroot", 0.0),
            Map.entry("Salted Sunflower Seeds", 0.0), Map.entry("Crystalized Moonlight", 0.0),
            Map.entry("Floral Gelatin", 0.0), Map.entry("Seasoning", 0.0),
            Map.entry("Purse", 1.0));

    public static final Map<String, String> BAZAAR_MAPPING = Map.ofEntries(

            // Crops
            Map.entry("Wheat", "WHEAT"), Map.entry("Enchanted Wheat", "ENCHANTED_WHEAT"), Map.entry("Enchanted Hay Bale", "ENCHANTED_HAY_BALE"),
            Map.entry("Seeds", "SEEDS"), Map.entry("Enchanted Seeds", "ENCHANTED_SEEDS"), Map.entry("Box of Seeds", "BOX_OF_SEEDS"),
            Map.entry("Potato", "POTATO_ITEM"), Map.entry("Enchanted Potato", "ENCHANTED_POTATO"), Map.entry("Enchanted Baked Potato", "ENCHANTED_BAKED_POTATO"),
            Map.entry("Carrot", "CARROT_ITEM"), Map.entry("Enchanted Carrot", "ENCHANTED_CARROT"), Map.entry("Enchanted Golden Carrot", "ENCHANTED_GOLDEN_CARROT"),
            Map.entry("Melon Slice", "MELON"), Map.entry("Enchanted Melon Slice", "ENCHANTED_MELON"), Map.entry("Enchanted Melon", "ENCHANTED_MELON_BLOCK"),
            Map.entry("Pumpkin", "PUMPKIN"), Map.entry("Enchanted Pumpkin", "ENCHANTED_PUMPKIN"), Map.entry("Polished Pumpkin", "POLISHED_PUMPKIN"),
            Map.entry("Sugar Cane", "SUGAR_CANE"), Map.entry("Enchanted Sugar", "ENCHANTED_SUGAR"), Map.entry("Enchanted Sugar Cane", "ENCHANTED_SUGAR_CANE"),
            Map.entry("Cactus", "CACTUS"), Map.entry("Enchanted Cactus Green", "ENCHANTED_CACTUS_GREEN"), Map.entry("Enchanted Cactus", "ENCHANTED_CACTUS"),
            Map.entry("Red Mushroom", "RED_MUSHROOM"), Map.entry("Enchanted Red Mushroom", "ENCHANTED_RED_MUSHROOM"), Map.entry("Enchanted Red Mushroom Block", "ENCHANTED_HUGE_MUSHROOM_2"),
            Map.entry("Brown Mushroom", "BROWN_MUSHROOM"), Map.entry("Enchanted Brown Mushroom", "ENCHANTED_BROWN_MUSHROOM"), Map.entry("Enchanted Brown Mushroom Block", "ENCHANTED_HUGE_MUSHROOM_1"),
            Map.entry("Cocoa Beans", "INK_SACK"), Map.entry("Enchanted Cocoa Beans", "ENCHANTED_COCOA"), Map.entry("Enchanted Cookie", "ENCHANTED_COOKIE"),
            Map.entry("Nether Wart", "NETHER_STALK"), Map.entry("Enchanted Nether Wart", "ENCHANTED_NETHER_STALK"), Map.entry("Mutant Nether Wart", "MUTANT_NETHER_STALK"),
            Map.entry("Sunflower", "DOUBLE_PLANT"), Map.entry("Enchanted Sunflower", "ENCHANTED_SUNFLOWER"), Map.entry("Compacted Sunflower", "COMPACTED_SUNFLOWER"),
            Map.entry("Moonflower", "MOONFLOWER"), Map.entry("Enchanted Moonflower", "ENCHANTED_MOONFLOWER"), Map.entry("Compacted Moonflower", "COMPACTED_MOONFLOWER"),
            Map.entry("Wild Rose", "WILD_ROSE"), Map.entry("Enchanted Wild Rose", "ENCHANTED_WILD_ROSE"), Map.entry("Compacted Wild Rose", "COMPACTED_WILD_ROSE"),
    
            // Pest Items
            Map.entry("Beady Eyes", "BEADY_EYES"),
            Map.entry("Chirping Stereo", "CHIRPING_STEREO"),
            Map.entry("Sunder VI Book", "ENCHANTMENT_SUNDER_6"),
            Map.entry("Clipped Wings", "CLIPPED_WINGS"),
            Map.entry("Bookworm's Favorite Book", "BOOKWORM_BOOK"),
            Map.entry("Atmospheric Filter", "ATMOSPHERIC_FILTER"),
            Map.entry("Wriggling Larva", "WRIGGLING_LARVA"),
            Map.entry("Pesterminator I Book", "ENCHANTMENT_PESTERMINATOR_1"),
            Map.entry("Squeaky Toy", "SQUEAKY_TOY"),
            Map.entry("Squeaky Mousemat", "SQUEAKY_MOUSEMAT"),
            Map.entry("Fire in a Bottle", "FIRE_IN_A_BOTTLE"),
            Map.entry("Vermin Vaporizer Chip", "VERMIN_VAPORIZER_GARDEN_CHIP"),
            Map.entry("Mantid Claw", "MANTID_CLAW"),
            Map.entry("Overclocker 3000", "OVERCLOCKER_3000"),
            Map.entry("Dung", "DUNG"),
            Map.entry("Honey Jar", "HONEY_JAR"),
            Map.entry("Plant Matter", "PLANT_MATTER"),
            Map.entry("Tasty Cheese", "CHEESE_FUEL"),
            Map.entry("Compost", "COMPOST"),
            Map.entry("Jelly", "JELLY"),
            Map.entry("Pest Shard", "SHARD_PEST"),

            //Misc
            Map.entry("Cropie", "CROPIE"),
            Map.entry("Squash", "SQUASH"),
            Map.entry("Fermento", "FERMENTO"),
            Map.entry("Helianthus", "HELIANTHUS"),
            Map.entry("Ashwreath", "ASHWREATH"), Map.entry("Choconut", "CHOCONUT"),
            Map.entry("Dustgrain", "DUSTGRAIN"), Map.entry("Gloomgourd", "GLOOMGOURD"),
            Map.entry("Lonelily", "LONELILY"), Map.entry("Scourroot", "SCOURROOT"),
            Map.entry("Shadevine", "SHADEVINE"), Map.entry("Veilshroom", "VEILSHROOM"),
            Map.entry("Witherbloom", "WITHERBLOOM"), Map.entry("Chocoberry", "CHOCOBERRY"),
            Map.entry("Cindershade", "CINDERSHADE"), Map.entry("Coalroot", "COALROOT"),
            Map.entry("Creambloom", "CREAMBLOOM"), Map.entry("Duskbloom", "DUSKBLOOM"),
            Map.entry("Thornshade", "THORNSHADE"), Map.entry("Blastberry", "BLASTBERRY"),
            Map.entry("Cheesebite", "CHEESEBITE"), Map.entry("Chloronite", "CHLORONITE"),
            Map.entry("Do-not-eat-shroom", "DO_NOT_EAT_SHROOM"), Map.entry("Fleshtrap", "FLESHTRAP"),
            Map.entry("Magic Jellybean", "MAGIC_JELLYBEAN"), Map.entry("Noctilume", "NOCTILUME"),
            Map.entry("Snoozling", "SNOOZLING"), Map.entry("Soggybud", "SOGGYBUD"),
            Map.entry("Chorus Fruit", "CHORUS_FRUIT"), Map.entry("PlantBoy Advance", "PLANTBOY_ADVANCE"),
            Map.entry("Puffercloud", "PUFFERCLOUD"), Map.entry("Shellfruit", "SHELLFRUIT"),
            Map.entry("Startlevine", "STARTLEVINE"), Map.entry("Stoplight Petal", "STOPLIGHT_PETAL"),
            Map.entry("Thunderling", "THUNDERLING"), Map.entry("Turtlellini", "TURTLELLINI"),
            Map.entry("Zombud", "ZOMBUD"), Map.entry("All-in Aloe", "ALL_IN_ALOE"),
            Map.entry("Devourer", "DEVOURER"), Map.entry("Glasscorn", "GLASSCORN"),
            Map.entry("Godseed", "GODSEED"),
            Map.entry("Phantomleaf", "PHANTOMLEAF"), Map.entry("Timestalk", "TIMESTALK"),

            Map.entry("Tool EXP Capsule", "TOOL_EXP_CAPSULE"),
            Map.entry("ENCHANTMENT_GREEN_THUMB_1", "ENCHANTMENT_GREEN_THUMB_1"),

            // Visitor / Rare Drops Mappings
            Map.entry("Overgrown Grass", "OVERGROWN_GRASS"),
            Map.entry("Flowering Bouqet", "FLOWERING_BOUQUET"),
            Map.entry("Green Bandana", "GREEN_BANDANA"),
            Map.entry("Hypercharge Chip", "HYPERCHARGE_GARDEN_CHIP"),
            Map.entry("Quickdraw Chip", "QUICKDRAW_GARDEN_CHIP"),
            Map.entry("Superboom TNT", "SUPERBOOM_TNT"),
            Map.entry("Green Candy", "GREEN_CANDY"),
            Map.entry("Purple Candy", "PURPLE_CANDY"),
            Map.entry("Ice Essence", "ESSENCE_ICE"),
            Map.entry("Diamond Essence", "ESSENCE_DIAMOND"),
            Map.entry("Gold Essence", "ESSENCE_GOLD"),
            Map.entry("Jacob's Ticket", "JACOBS_TICKET"),
            Map.entry("Turbo-Cacti I Book", "ENCHANTMENT_TURBO_CACTUS_1"),
            Map.entry("Turbo-Cane I Book", "ENCHANTMENT_TURBO_CANE_1"),
            Map.entry("Turbo-Carrot I Book", "ENCHANTMENT_TURBO_CARROT_1"),
            Map.entry("Turbo-Cocoa I Book", "ENCHANTMENT_TURBO_COCO_1"),
            Map.entry("Turbo-Melon I Book", "ENCHANTMENT_TURBO_MELON_1"),
            Map.entry("Turbo-Moonflower I Book", "ENCHANTMENT_TURBO_MOONFLOWER_1"),
            Map.entry("Turbo-Mushrooms I Book", "ENCHANTMENT_TURBO_MUSHROOMS_1"),
            Map.entry("Turbo-Potato I Book", "ENCHANTMENT_TURBO_POTATO_1"),
            Map.entry("Turbo-Pumpkin I Book", "ENCHANTMENT_TURBO_PUMPKIN_1"),
            Map.entry("Turbo-Rose I Book", "ENCHANTMENT_TURBO_ROSE_1"),
            Map.entry("Turbo-Sunflower I Book", "ENCHANTMENT_TURBO_SUNFLOWER_1"),
            Map.entry("Turbo-Warts I Book", "ENCHANTMENT_TURBO_WARTS_1"),
            Map.entry("Turbo-Wheat I Book", "ENCHANTMENT_TURBO_WHEAT_1"),
            Map.entry("Cultivating I Book", "ENCHANTMENT_CULTIVATING_1"),
            Map.entry("Delicate V Book", "ENCHANTMENT_DELICATE_5"),
            Map.entry("Replenish I Book", "ENCHANTMENT_REPLENISH_1"),
            Map.entry("Dedication IV Book", "ENCHANTMENT_DEDICATION_4"),
            Map.entry("Jungle Key", "JUNGLE_KEY"),
            Map.entry("Pet Cake", "PET_CAKE"),
            Map.entry("Fine Flour", "FINE_FLOUR"),
            Map.entry("Arachne Fragment", "ARACHNE_FRAGMENT"),

            // AH Items Mappings
            Map.entry("Biofuel", "BIOFUEL"),
            Map.entry("Farming Exp Boost", "PET_ITEM_FARMING_SKILL_BOOST_UNCOMMON"),
            Map.entry("Harvest Harbinger V Potion", "POTION_harvest_harbinger"),
            Map.entry("Velvet Top Hat", "VELVET_TOP_HAT"),
            Map.entry("Cashmere Hat", "CASHMERE_JACKET"),
            Map.entry("Satin Trousers", "SATIN_TROUSERS"),
            Map.entry("Oxford Shoes", "OXFORD_SHOES"),
            Map.entry("Space Helmet", "DCTR_SPACE_HELM"),
            Map.entry("Wild Strawberry Dye", "DYE_WILD_STRAWBERRY"),
            Map.entry("Copper Dye", "DYE_COPPER"),
            Map.entry("Clouds Rune I", "RUNE_CLOUDS"),
            Map.entry("Fire Spiral Rune I", "RUNE_FIRE_SPIRAL"),
            Map.entry("Gem Rune I", "RUNE_GEM"),
            Map.entry("Golden Rune I", "RUNE_GOLDEN"),
            Map.entry("Hearts Rune I", "RUNE_HEARTS"),
            Map.entry("Hot Rune I", "RUNE_HOT"),
            Map.entry("Lava Rune I", "RUNE_LAVA"),
            Map.entry("Lightning Rune I", "RUNE_LIGHTNING"),
            Map.entry("Magical Rune I", "RUNE_MAGIC"),
            Map.entry("Music Rune I", "RUNE_MUSIC"),
            Map.entry("Rainbow Rune I", "RUNE_RAINBOW"),
            Map.entry("Redstone Rune I", "RUNE_REDSTONE"),
            Map.entry("Smokey Rune I", "RUNE_SMOKEY"),
            Map.entry("Snow Rune I", "RUNE_SNOW"),
            Map.entry("Sparkling Rune I", "RUNE_SPARKLING"),
            Map.entry("Wake Rune I", "RUNE_WAKE"),
            Map.entry("White Spiral Rune I", "RUNE_WHITE_SPIRAL"),
            Map.entry("Zap Rune I", "RUNE_ZAP"),

            // Feast Items (Harvest Feast base drops; bazaar IDs follow Hypixel's
            // standard UPPER_SNAKE_CASE convention. Items not yet listed on
            // bazaar will simply price at 0 — same as the existing Misc Drops
            // without bazaar listings.)
            Map.entry("Cornucopia", "CORNUCOPIA"),
            Map.entry("Carrot Zest", "CARROT_ZEST"),
            Map.entry("Deepfries", "DEEPFRIES"),
            Map.entry("Aggourdian", "AGGOURDIAN"),
            Map.entry("Cane Knot", "CANE_KNOT"),
            Map.entry("Melon Juice", "MELON_JUICE"),
            Map.entry("Cactus Flower", "CACTUS_FLOWER"),
            Map.entry("Designer Coffee Beans", "DESIGNER_COFFEE_BEANS"),
            Map.entry("Feastfungus", "FEASTFUNGUS"),
            Map.entry("Botroot", "BOTROOT"),
            Map.entry("Salted Sunflower Seeds", "SALTED_SUNFLOWER_SEEDS"),
            Map.entry("Crystalized Moonlight", "CRYSTALIZED_MOONLIGHT"),
            Map.entry("Floral Gelatin", "FLORAL_GELATIN"),

            // Pest drops added in 0.24.4
            Map.entry("Locust Larva", "LOCUST_LARVA"));
}
