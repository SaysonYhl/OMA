# Goal
This activity creates the data access layer of this application. During this guide, the following should be achieved using the database:

- Create and save an order
- Fetch orders
- Update order status
- Fetch products

# Prerequisites
1. Install PostgreSQL from an official repository

    ```
    sudo apt install wget ca-certificates
    ```

    ```
    wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
    sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ $(lsb_release -cs)-pgdg main" >> /etc/apt/sources.list.d/pgdg.list'
    ```

    ```
    sudo apt update
    apt install postgresql postgresql-contrib
    ```

   After the installation you may double-check that postgresql daemon is active.

    ```
    service postgresql status
    ```
   ![Untitled (1)](https://user-images.githubusercontent.com/56243713/184961488-a1f0c6a1-3d4f-4018-a282-3f6efe6faa3b.png)

2. Use Postgresql Command Line Tool

    ```
    sudo -u postgres psql
    ```

   A “psql” command-line client tool is used to interact with the database engine. You should invoke it as a “postgres” user to start an interactive session with your local database. When you’re connected, the prompt should change to:

    ```
    postgres=#
    ```

   Since the default “postgres” user does not have a password, you should set it yourself. Let's set it to `root` for uniformity's sake.

    ```
    postgres=# \password postgres
    ```

   You can continue to explore the psql command line tool using this guide: [https://www.geeksforgeeks.org/postgresql-psql-commands/](https://www.geeksforgeeks.org/postgresql-psql-commands/)

3. Create database

    ```
    postgres=# CREATE DATABASE order_management;
    ```

   Try connecting to newly created database.

    ```
    postgres=# \c order_management
    ```

4. (Optional) Connect via GUI client

   **PgAdmin** is probably the most popular option for PostgreSQL.

   You can find the download and installation for PgAdmin [here](https://www.pgadmin.org/download/) and a more comprehensive guide to connecting [here](https://www.cherryservers.com/blog/how-to-install-and-setup-postgresql-server-on-ubuntu-20-04#connect-via-gui-client).



## Create **Entities**

The following are the entities in this application:

- Order
- OrderDetails
- Product

![Untitled](https://user-images.githubusercontent.com/56243713/184961290-f92fbb0e-b450-4965-ba81-e097601deb44.png)


### Order Entity

1. Add the annotations
- @Entity - represents the table to be created in the database
- @Id - represents the primary key
- @GeneratedValue and @SequenceGenerator - defines the primary key value
- @Column - represents the column definition of the entity/table
2. Add a no-arg constructor
    - Why? [from Hibernate doc](https://docs.jboss.org/hibernate/orm/5.5/quickstart/html_single/#hibernate-gsg-tutorial-basic-entity):

      > The no-argument constructor, which is also a JavaBean convention, is a requirement for all persistent classes. Hibernate needs to create objects for you, using Java Reflection. The constructor can be private. However, package or public visibility is required for runtime proxy generation and efficient data retrieval without bytecode instrumentation.
>

```java
@Entity(name = "orders") // Note: Used "orders" because "order" is a reserved word in postgres
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_sequence")
  @SequenceGenerator(name = "order_sequence", sequenceName = "order_sequence", allocationSize = 1)
  Long id;

  @Setter
  List<OrderDetails> orderDetailsList = new ArrayList<>();

  @Setter
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  OrderStatus status;

  @Setter
  @Column(nullable = false)
  double totalCost;

  @Setter
  @Column(nullable = false)
  LocalDateTime dateOrdered;

	public Order() {

	}
}
```

### Product Entity

```java
@Entity
@Getter
@EqualsAndHashCode(of = "id")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_sequence")
  @SequenceGenerator(name = "product_sequence", sequenceName = "product_sequence", allocationSize = 1)
  Long id;

  @Column(nullable = false)
  String name;

  @Column(nullable = false)
  String description;

  @Column(nullable = false)
  int productQuantity;

  @Column(nullable = false)
  double unitPrice;

	public Product() {
	
	}

}
```

### Order Details Entity

```java
@Entity
@Getter
public class OrderDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_details_sequence")
  @SequenceGenerator(name = "order_details_sequence", sequenceName = "order_details_sequence", allocationSize = 1)
  long id;

  @Setter
  Long orderId;

  Long productId;

  @Column(nullable = false)
  int quantity;

	public OrderDetails() {
	
	}
}
```

### Entity Associations

In `Order` entity, add the `OneToMany` annotation to `orderDetailsList` to create the association to `OrderDetails`.

Create `addToOrderDetails` method.

```java
@Entity(name = "orders") // Note: Used "orders" because "order" is a reserved word in postgres
@Getter
public class Order {

	...

  @Setter
  @OneToMany(targetEntity = OrderDetails.class, mappedBy = "order", cascade = {CascadeType.ALL})
  List<OrderDetails> orderDetailsList = new ArrayList<>();

	...

public void addToOrderDetails(OrderDetails orderDetails) {
    orderDetailsList.add(orderDetails);
    if (orderDetails.getOrder() != this) {
      orderDetails.setOrder(this);
    }
  }
}
```

Update `OrderDetails`entity to reference `Order` and `Product` entity and also update constructor.

Add the `ManyToOne` annotation to `order` and `JoinColumn` annotation to add the link to the `orders` table. Also add `ManyToOne` annotation to `product`.

If the `OneToMany`uses a foreign key in the target object's table JPA requires that the relationship be bi-directional (inverse `ManyToOne` relationship must be defined in the target object), and the source object must use the `mappedBy` attribute to define the mapping. Since relationship is bi-directional so, as the application updates one side of the relationship, the other side should also get updated, and be in sync. In JPA, as in Java in general it is the responsibility of the application, or the object model to maintain relationships. If your application adds to one side of a relationship, then it must add to the other side.

```java
@Entity
@Getter
public class OrderDetails {

  @Setter
  @ManyToOne(targetEntity = Order.class, cascade = {CascadeType.ALL})
  @JoinColumn(name="order_id")
  Order order;

	@ManyToOne(targetEntity = Product.class)
  Product product;

	...
	
	public OrderDetails(Order order, Product product, Integer quantity) {
    this.product = product;
    this.quantity = quantity;
    this.order = order;
    if (!order.getOrderDetailsList().contains(this)) {
      order.getOrderDetailsList().add(this);
    }
  }
}
```

## Repositories

- Create `OrderRepository` and `ProductRepository`

```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
```

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
```

## Fetch all products

### Pagination

To implement the pagination, we need to:

1. Create or obtain a *PageRequest* object, which is an implementation of the *Pageable* interface
2. Pass the *PageRequest* object as an argument to the repository method we intend to use

We can create a *PageRequest* object by passing in the requested page number and the page size.

In `ProductService`, update `fetchProducts` method

```java
Page<Product> fetchPagedProducts(int max, int page) {
    Pageable pageable = PageRequest.of(page, max);

    return productRepository.findAll(pageable);
  }
```

The *findAll(Pageable pageable)* method by default returns a *Page<T>* object.

However, **we can choose to return either a *Page<T>,* a *Slice<T>,* or a *List<T>* from any of our custom methods returning paginated data**.

### Update controller implementation to return Page<T>

We need to use `getContent` implemented by *Slice<T> (extend by Page<T>)* to return a list.

```java
@GetMapping("/api/v1/product")
  public PageResponse<ProductResponse> getProducts(
      @RequestParam(value = "max", defaultValue = "2") int max,
      @RequestParam(value = "page", defaultValue = "1") int page) {
    Page<Product> products = productService.fetchProducts(max, page);

    List<ProductResponse> productResponseList =
        products.getContent().stream().map(ProductResponse::new).collect(Collectors.toList());

    return new PageResponse<>(productService.fetchTotalProductCount(), page, productResponseList);
  }
```

## Fetch a product
- First, we need to populate the product table.
  In `ProductService`, create method `createInitialProducts`. Update constructor
```
  @Autowired
  public ProductService(List<Product> productList, ProductRepository productRepository) {
    this.productList = productList;
    this.productRepository = productRepository;
    createInitialProducts();
  }
```
```
  private void createInitialProducts() {
    productRepository.saveAll(this.productList);
  }
```

In `ProductService`, update `fetchProductById` method

```java
public Optional<Product> fetchProductById(Long id) {
  return productRepository.findById(id);
}
```

The *findById(id)* method by default returns *Optional<T>* object.

## Fetch total product count

In `ProductService`, update `fetchTotalProductCount` method

```java
int fetchTotalProductCount() {
    return (int) productRepository.count();
}
```

After creating the implementation for the `Product` entity, test the APIs again and check if records are saved in the database.

## Fetch orders

In `OrderRepository`, add `findByIdAndStatus` method. This will query by order id and status.

```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

	long countAllByStatus(OrderStatus status);

}
```

In `OrderService`, update `fetchOrders` method

```java
Page<Order> fetchOrders(int max, int page) {
    Pageable pageable = PageRequest.of(page, max);

    return orderRepository.findAllByStatus(OrderStatus.CHECKED_OUT, pageable);
  }
```

## Create order and order details

To create a new order, we have to do the ff:

- Order object
- Create OrderDetails object from request
- Calculate order total cost
- Deduct product quantity from product list
- Set order total
- Set order status
- Set date ordered
- Save order

In `OrderService`, update `createNewOrder`

```java
public Order createNewOrder(List<OrderDetailsRequest> orderDetailsRequests) {
    Order order = new Order();

    List<OrderDetails> orderDetailsList =
        orderDetailsRequests.stream().map(orderDetailsRequest ->
            createOrderDetails(order, orderDetailsRequest)).collect(Collectors.toList());

    orderProductQuantityModifier.deductProductQuantityBasedOnOrder(orderDetailsList);
    double totalCost = orderCalculator.calculateTotalCost(orderDetailsList);
    
    order.setTotalCost(totalCost);
    order.setStatus(OrderStatus.CHECKED_OUT);
    order.setDateOrdered(LocalDateTime.now());

    return orderRepository.save(order);
  }
```

In `OrderService`, update `createOrderDetails`

```java
private OrderDetails createOrderDetails(Order order, OrderDetailsRequest orderDetailsRequest) {
    OrderDetails orderDetails = new OrderDetails(order, getProductById(orderDetailsRequest.getProductId()), orderDetailsRequest.getQuantity());
    order.addToOrderDetails(orderDetails);
    return orderDetails;
  }
```

## Cancel order
To cancel an new order, we have to do the ff:
- Fetch checked-out order
- Check if order can still be cancelled
- Update order status
- Add product quantity

In `OrderRepository`, add `findByIdAndStatus` method
```
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	Optional<Order> findByIdAndStatus(Long orderId, OrderStatus status);

	Optional<Order> findAllByStatus(Long orderId, OrderStatus status);

	long countAllByStatus(OrderStatus status);

}
```

In `OrderService`, update `fetchCheckedOutOrderById` method

```
  Optional<Order> fetchCheckedOutOrderById(long orderId) {
    return orderRepository.findByIdAndStatus(orderId, OrderStatus.CHECKED_OUT);
  }

```

In `OrderService`, update `cancelOrder` method
```
  @Transactional
  void cancelOrder(Order order) throws UnableToCancelOrderException {
    if (order.canBeCancelledBasedOnOrderDate()) {
      order.cancel();
      orderRepository.save(order);
      orderProductQuantityModifier.addProductQuantityBasedOnCancelledOrder(
          order.getOrderDetailsList());
    } else {
      throw new UnableToCancelOrderException(
          "Can't cancel order that was ordered more than 3 days ago");
    }
  }
```

