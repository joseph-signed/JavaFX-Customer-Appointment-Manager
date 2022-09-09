package Program;

import Models.Customer;

/**
 * Handles the global static SelectedCustomer object for use when passing information between controllers.
 */
public class SelectedCustomer {
    private static Customer Customer;

    public static Customer GetSelectedCustomer() { return Customer; }
    public static void SetSelectedCustomer(Customer selectedCustomer) { Customer = selectedCustomer; }
}
