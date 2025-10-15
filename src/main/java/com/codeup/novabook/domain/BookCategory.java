package com.codeup.novabook.domain;

/**
 * Enumeration of book categories available in the library system.
 * <p>
 * This enum represents the standardized categories for book classification.
 * Categories are stored as ENUM type in the database (book_category).
 * </p>
 * 
 * <p><b>Usage in UI:</b></p>
 * <pre>{@code
 * ComboBox<BookCategory> categoryCombo = new ComboBox<>();
 * categoryCombo.getItems().addAll(BookCategory.values());
 * categoryCombo.setConverter(new StringConverter<BookCategory>() {
 *     public String toString(BookCategory category) {
 *         return category != null ? category.getDisplayName() : "";
 *     }
 *     public BookCategory fromString(String string) {
 *         return BookCategory.fromDisplayName(string);
 *     }
 * });
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 */
public enum BookCategory {
    LITERATURE("Literature"),
    SCIENCE_FICTION("Science Fiction"),
    CHILDREN("Children"),
    CLASSICS("Classics"),
    FICTION("Fiction"),
    FANTASY("Fantasy"),
    ROMANCE("Romance"),
    MYSTERY("Mystery"),
    BIOGRAPHY("Biography"),
    HISTORY("History"),
    SCIENCE("Science"),
    TECHNOLOGY("Technology"),
    BUSINESS("Business"),
    SELF_HELP("Self-Help"),
    ART("Art"),
    TRAVEL("Travel"),
    COOKING("Cooking"),
    POETRY("Poetry"),
    DRAMA("Drama"),
    RELIGION("Religion"),
    PHILOSOPHY("Philosophy"),
    EDUCATION("Education"),
    COMICS("Comics"),
    HORROR("Horror"),
    OTHER("Other");
    
    private final String displayName;
    
    /**
     * Constructor for BookCategory enum.
     * 
     * @param displayName the human-readable display name
     */
    BookCategory(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the human-readable display name of the category.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Finds a BookCategory by its display name.
     * <p>
     * This method is useful for converting user-facing strings back to enum values.
     * </p>
     * 
     * @param displayName the display name to search for
     * @return the matching BookCategory, or null if not found
     */
    public static BookCategory fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }
        
        for (BookCategory category : BookCategory.values()) {
            if (category.displayName.equalsIgnoreCase(displayName.trim())) {
                return category;
            }
        }
        return null;
    }
    
    /**
     * Converts database enum name to BookCategory.
     * <p>
     * Database stores as: SCIENCE_FICTION, SELF_HELP, etc.
     * </p>
     * 
     * @param dbValue the database enum value
     * @return the matching BookCategory
     * @throws IllegalArgumentException if dbValue is invalid
     */
    public static BookCategory fromDatabaseValue(String dbValue) {
        if (dbValue == null || dbValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Database value cannot be null or empty");
        }
        
        try {
            return BookCategory.valueOf(dbValue.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid book category: " + dbValue, e);
        }
    }
    
    /**
     * Converts BookCategory to database enum value.
     * 
     * @return the database enum value (e.g., "SCIENCE_FICTION")
     */
    public String toDatabaseValue() {
        return this.name();
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
