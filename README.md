# Restful Web Service Design Exercise

This exercise is designed to help you apply the concepts you've learned regarding restful web services.

In this exercise, we'll design a simple web service for order and product management.

### Requirements
- Users should be able to create and cancel orders. Note: Cancelled orders are considered "deleted" and are not viewable.
- Users should be able to view orders and filter orders for a specific day.
- Users should be able to add and update products.
- Users should be able to view a single product.
- Users should be able to view products and filter them by name.

### Models
- Product
    - name
    - description
    - product quantity
    - unit price

- Order
    - list of order details
    - date ordered
    - status (CHECKED_OUT, CANCELLED)
    - total cost

- Order Details
    - product id
    - quantity
