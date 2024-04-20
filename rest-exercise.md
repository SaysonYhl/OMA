### Requirements
- Create an endpoint for adding/creating a product.
- Create an endpoint for updating a product.
- For the existing list product endpoint, add a functionality to be able to optionally filter by name.
    - It should return products with names that contain the value of the "name". For example, if value of filter is "phone", products returned can include "phone case", "headphones", "xylophone".
    - Filtering should NOT be case sensitive.
- Business rule for product is that each product must have a unique name.
    - The application should respond appropriately if a request violates this rule.
    - The response body should show an appropriate "errorCode" and "errorMessage".
- Create unit tests for the changes.


### Bonus
- For endpoints with pagination parameters, if "max" or "page"  value is less than 1, application must respond with an appropriate error.
    - Status code for this scenario should be `400`.
    - The response body should show an appropriate "errorCode" and "errorMessage".
- For endpoints with request body, application should be able to check the required fields.
    - If a required field is missing, response status code should be `400` and response body should show appropriate `errorCode` and `errorMessage`.
- For the existing list order endpoint, add a functionality to be able to optionally filter by day of date ordered.
    - If value of given "dateOrdered" is `2024-01-02`, application must return all orders that were created within that date.