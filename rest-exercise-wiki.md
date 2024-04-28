# Goal
Create an order management application. Users would be able to view products and create orders. Users would also be able to cancel their order.

For this activity, we won't be using a database yet. The objects will only be stored in memory.

# Project
Clone the [OrderManagementApplication](https://github.com/ITPC-115/OrderManagementApplication) project.

The next steps below will walk us through the creation of the order management application.

## Add the dependency (Can be skipped)
Add the following in the dependencies under `build.gradle`
```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'

compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

The starter web dependency includes a set of dependencies such as:
- spring web mvc - this would allow us to create our web service
- springboot starter tomcat - this provides an embedded web server (tomcat) needed for our HTTP application
- springboot starter json - this provides automatic mapping of objects to/from json

## Add an initial sample controller

```java
@RestController
public class TestController {

    @GetMapping("/api/v1/test")
    public String test() {
        return "Hello World!";
    }
}
```

The `@RestController` marks this class as a "controller". This allows us to define endpoints and map them to methods in this class. The `@RestController` annotation also gives the capability of automatically converting our returned object as an HTTP response body.

The `@GetMapping` annotation targets a method and it allows us to define an endpoint and map it to the annotated method. It also assigns that the endpoint is accessed via the `GET` HTTP method.

Run the application via `bootRun`. Visit `http://localhost:8080/api/v1/test` in the browser.

Use postman to send the request and view the response.

## Add the product controller

### Add get product by id endpoint

Add the following `ProductController` class under the `product` package:

```java
@RestController
public class ProductController {

  private final ProductService productService;

  @Autowired
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/api/v1/product/{id}")
  public Product getProduct(@PathVariable Long id) {
    return this.productService.fetchProductById(id).get();
  }
}
```

The `@PathVariable` annotation provides us with a reference to a variable in our URI path.

Add the following `ProductService` class under the `product` package:

```java
@Service
public class ProductService {

  private final List<Product> productList;

  @Autowired
  public ProductService(List<Product> productList) {
    this.productList = productList;
  }

  public Optional<Product> fetchProductById(Long id) {
    return productList.stream().filter(it -> Objects.equals(it.getId(), id)).findFirst();
  }
}
```

Add the following configuration class in the same level as the Application class:

```java
@Configuration
public class DatastoreConfig {

  @Bean
  public List<Product> getProductList() {
    Product product1 =
        new Product(1L, "Product A", "This is a description for product A", 1000, 149.99);
    Product product2 =
        new Product(2L, "Product B", "This is a description for product B", 300, 450.00);
    Product product3 =
        new Product(3L, "Product C", "This is a description for product C", 4000, 1499.99);
    List<Product> productList = new ArrayList<>();
    productList.add(product1);
    productList.add(product2);
    productList.add(product3);
    return productList;
  }
}
```

Update the `Product` class:

```java
public class Product {

  @Getter Long id;
  @Getter String name;
  @Getter String description;
  @Getter int productQuantity;
  @Getter double unitPrice;

  public Product(Long id, String name, String description, int productQuantity, double unitPrice) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.productQuantity = productQuantity;
    this.unitPrice = unitPrice;
  }
}
```

Run the application. Test the added endpoints in postman.

Notice what happens when we specify a product id that doesn't exist. We'll fix that later.

### Using a different object for the response

**Why would we want to do this?**

Right now in our code, our controller is tightly coupled to our model. This is because the object that we're using in the controller response is directly the model.

Let's create a separate class/object to represent the product response.

```java
class ProductResponse {

  @Getter private Long id;
  @Getter private String name;
  @Getter private String description;
  @Getter private int productQuantity;
  @Getter private double unitPrice;
  @Getter private Currency currency;

  ProductResponse(Product product) {
    this.id = product.getId();
    this.name = product.getName();
    this.description = product.getDescription();
    this.productQuantity = product.getProductQuantity();
    this.unitPrice = product.getUnitPrice();
    this.currency = Currency.getInstance("PHP");
  }
}
```

Now, let's update our controller:

```java
  public ProductResponse getProduct(@PathVariable Long id) {
    Product product = this.productService.fetchProductById(id).get();
    return new ProductResponse(product);
  }
```

Run the application and test the changes.

Now that we've finished the endpoint for fetching the product by id, let's implement the next endpoint.

### Add get product list endpoint

Add the new endpoint in the controller:

```java
  @GetMapping("/api/v1/product")
  public List<ProductResponse> getProducts() {
    List<Product> products = productService.fetchProducts();
    return products.stream().map(ProductResponse::new).collect(Collectors.toList());
  }
```

and the new method in the service:

```java
  List<Product> fetchProducts() {
    return productList;
  }
```

Run the application and test the new endpoint.


#### Add pagination

Let's alter our service to consider pagination:

```java
  List<Product> fetchProducts(int max, int page) {
    List<List<Product>> productGroups = Lists.partition(productList, max);
    return productGroups.get(page - 1);
  }

  int fetchTotalProductCount() {
    return productList.size();
  }
```

The service is actually using a library to easily group the product list by page. Let's add that library in our dependencies:

```groovy
	implementation 'com.google.guava:guava:31.1-jre'
```

We want to use a different object to represent our paginated response. Create the following class under `com.itel.ordermanagement.web` package.

```java
public class PageResponse<T> {
  @Getter private int totalCount;

  @Getter private int pageNumber;

  @Getter private List<T> content;

  public PageResponse(int totalCount, int pageNumber, List<T> content) {
    this.totalCount = totalCount;
    this.pageNumber = pageNumber;
    this.content = content;
  }
}
```

Now let's update our controller to add pagination:

```java
  @GetMapping("/api/v1/product")
  public PageResponse<ProductResponse> getProducts(
      @RequestParam Integer max, @RequestParam Integer page) {
    List<Product> products = productService.fetchProducts(max, page);

    List<ProductResponse> productResponseList =
        products.stream().map(ProductResponse::new).collect(Collectors.toList());

    return new PageResponse<>(productService.fetchTotalProductCount(), page, productResponseList);
  }
```

The `@RequestParam` annotation gives us access to the query parameters in the request.

Run the application and test the changes.

Notice what happens when the max and page parameters are not specified. By default, request params are required.

Let's set them as optional.

```java
  public PageResponse<ProductResponse> getProducts(
      @RequestParam(value = "max", defaultValue = "2") int max,
      @RequestParam(value = "page", defaultValue = "1") int page) {
    // code here
  }
```

Run the application and test the changes.

Lastly, notice what happens when we specify a page that's not existing. This is because of our logic in our service. Let's try to fix that.

```java
  List<Product> fetchProducts(int max, int page) {
    List<List<Product>> productGroups = Lists.partition(productList, max);
    if (productGroups.size() < page) {
      return Collections.emptyList();
    } else {
      return productGroups.get(page - 1);
    }
  }
```

Run the application and test the changes.

## Add the order controller

### Fetch orders endpoint

Add the new controller under the `order` package:

```java
@RestController
public class OrderController {

  private final OrderService orderService;

  @Autowired
  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping("/api/v1/order")
  public PageResponse<OrderResponse> fetchOrders(
      @RequestParam(value = "max", defaultValue = "3") int max,
      @RequestParam(value = "page", defaultValue = "1") int page) {
    List<Order> orders = orderService.fetchOrders(max, page);
    List<OrderResponse> orderResponseList =
        orders.stream().map(OrderResponse::new).collect(Collectors.toList());
    return new PageResponse<>(orderService.fetchTotalOrderCount(), page, orderResponseList);
  }
}
```

and the Service under the `order` package:

```java
public class OrderService {

  private final List<Order> orderList;

  @Autowired
  public OrderService(List<Order> orderList) {
    this.orderList = orderList;
  }

  List<Order> fetchOrders(int max, int page) {
    List<Order> checkedOutOrders =
        this.orderList.stream()
            .filter(it -> it.getStatus() == OrderStatus.CHECKED_OUT)
            .collect(Collectors.toList());
    List<List<Order>> orderGroups = Lists.partition(checkedOutOrders, max);
    if (orderGroups.size() < page) {
      return Collections.emptyList();
    } else {
      return orderGroups.get(page - 1);
    }
  }

  int fetchTotalOrderCount() {
    return (int)
        this.orderList.stream().filter(it -> it.getStatus() == OrderStatus.CHECKED_OUT).count();
  }
}
```

Add the following bean in the `DatastoreConfig`:
```java
  @Bean
  public List<Order> getOrderList() {
    return new ArrayList<>();
  }
```

Also create the response objects under the `order` package:

```java
class OrderResponse {
  @Getter private final Long id;
  @Getter private final List<OrderDetailsResponse> orderDetails;
  @Getter private final Double totalCost;
  @Getter private final LocalDateTime dateOrdered;

  OrderResponse(Order order) {
    this.id = order.getId();
    this.totalCost = order.getTotalCost();
    this.dateOrdered = order.getDateOrdered();
    this.orderDetails =
        order.getOrderDetailsList().stream()
            .map(OrderDetailsResponse::new)
            .collect(Collectors.toList());
  }
}
```

```java
class OrderDetailsResponse {
  @Getter private final Long productId;
  @Getter private final Integer quantity;

  OrderDetailsResponse(OrderDetails orderDetails) {
    this.productId = orderDetails.getProductId();
    this.quantity = orderDetails.getQuantity();
  }
}

```

And update the `Order` and `OrderDetails` objects, so we'd be able to access the fields:

```java
public class Order {

  @Getter Long id;
  @Getter List<OrderDetails> orderDetailsList;
  @Getter OrderStatus status;
  @Getter double totalCost;
  @Getter LocalDateTime dateOrdered;
  
}
```

```java
public class OrderDetails {

  @Getter long orderId;
  @Getter long productId;
  @Getter int quantity;
}
```

Test the changes.

Now that we have our get endpoint, let's move on to creating an order.

### Create order endpoint

Update the `Order` and `OrderDetails` class to add the constructor for the orders that will be created.

```java
  public Order(Long id, List<OrderDetails> orderDetailsList) {
    this.id = id;
    this.orderDetailsList = orderDetailsList;
    this.status = OrderStatus.CHECKED_OUT;
    this.dateOrdered = LocalDateTime.now();
  }
```

```java
  public OrderDetails(Long productId, Integer quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }
```


Let's add a method to our service to be able to create an order:

```java
  public Order createNewOrder(List<OrderDetailsRequest> orderDetailsRequests) {
    List<OrderDetails> orderDetailsList =
        orderDetailsRequests.stream().map(this::createOrderDetails).collect(Collectors.toList());
    Order order = new Order(this.orderList.size() + 1L, orderDetailsList);
    this.orderList.add(order);
    return order;
  }

  private OrderDetails createOrderDetails(OrderDetailsRequest orderDetailsRequest) {
    return new OrderDetails(orderDetailsRequest.getProductId(), orderDetailsRequest.getQuantity());
  }
```

We'll also be using an object to represent the request. Let's add the objects/classes:

```java
class OrderRequest {
  @Getter private List<OrderDetailsRequest> orderDetails;
}
```

```java
class OrderDetailsRequest {
  @Getter private Long productId;
  @Getter private Integer quantity;
}
```

Lastly, let's add the new endpoint in our controller:

```java
  @PostMapping("/api/v1/order")
  public OrderResponse createOrder(@RequestBody OrderRequest orderRequest) {
    Order order = orderService.createNewOrder(orderRequest.getOrderDetails());
    return new OrderResponse(order);
  }
```

The `@RequestBody` annotation automatically maps the http request into an object. By default, the format that can be mapped automatically is JSON.
The `@PostMapping` annotation is similar to `@GetMapping` but corresponds to `POST` HTTP method.

Since we're creating an object for this endpoint, we're using `POST`.

Run the application and test the changes. After creating an order, verify that it's been added by using the get endpoint.

The response for the endpoint is still `200 OK`. Let's change that to `201 CREATED`.

Add the following annotation to the corresponding controller method:

```java
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/api/v1/order")
  public OrderResponse createOrder(@RequestBody OrderRequest orderRequest) {
    //code here
  }

```

The `@ResponseStatus` annotation in the method would specify the response HTTP status code that would be returned if the process is successful.

Our create endpoint is working and able to create and save orders. However, we're still missing a few logic. Let's add those.

Create a new component to calculate the total cost under `order` package:

```java
@Component
public class OrderCalculator {

  private final ProductService productService;

  @Autowired
  public OrderCalculator(ProductService productService) {
    this.productService = productService;
  }

  double calculateTotalCost(List<OrderDetails> orderDetailsList) {
    return orderDetailsList.stream().mapToDouble(this::getPriceOfOrderDetail).sum();
  }

  private double getPriceOfOrderDetail(OrderDetails orderDetails) {
    double productPrice =
        productService
            .fetchProductById(orderDetails.getProductId())
            .map(Product::getUnitPrice)
            .orElse(0.0);
    return productPrice * orderDetails.getQuantity();
  }
}
```

And populate the total cost field of the order.

Change the `Order` constructor:

```java
  public Order(Long id, List<OrderDetails> orderDetailsList, double totalCost) {
    this.id = id;
    this.orderDetailsList = orderDetailsList;
    this.totalCost = totalCost;
    this.status = OrderStatus.CHECKED_OUT;
    this.dateOrdered = LocalDateTime.now();
  }
```

and usage in the service:

```java
    double totalCost = orderCalculator.calculateTotalCost(orderDetailsList);
    Order order = new Order(this.orderList.size() + 1L, orderDetailsList, totalCost);
```

Now,let's add a component to adjust the product's quantity based on the order. Let's add this under the `order` package:

```java
@Component
public class OrderProductQuantityModifier {

  private final ProductService productService;

  @Autowired
  public OrderProductQuantityModifier(ProductService productService) {
    this.productService = productService;
  }

  void deductProductQuantityBasedOnOrder(List<OrderDetails> orderDetailsList) {
    for (OrderDetails orderDetails : orderDetailsList) {
      Product product = productService.fetchProductById(orderDetails.getProductId()).get();
      product.deductProductQuantity(orderDetails.getQuantity());
    }
  }
}

```

And add the new method in the `Product` class:

```java
  public void deductProductQuantity(int amountToDeduct) {
    this.productQuantity = this.productQuantity - amountToDeduct;
  }
```

Now, let's update our order service to call the new method.

```java
  Order createNewOrder(List<OrderDetailsRequest> orderDetailsRequests) {
    List<OrderDetails> orderDetailsList =
        orderDetailsRequests.stream().map(this::createOrderDetails).collect(Collectors.toList());
    orderProductQuantityModifier.deductProductQuantityBasedOnOrder(orderDetailsList);
    //other code
  }
```

Run the application, and test the changes.

We could now move on to the last endpoint, which is to delete/cancel an order.

### Cancel order endpoint

Let's add a method in our `Order` class to cancel an order.

```java
  void cancel() {
    this.status = OrderStatus.CANCELLED;
  }
```

Let's add methods in our order service to be able to fetch an order and to cancel a specific order.

```java
  Optional<Order> fetchCheckedOutOrderById(long orderId) {
    return orderList.stream()
        .filter(it -> it.getId() == orderId && it.getStatus() == OrderStatus.CHECKED_OUT)
        .findAny();
  }

  void cancelOrder(Order order) {
    order.cancel();
    orderProductQuantityModifier.addProductQuantityBasedOnCancelledOrder(
        order.getOrderDetailsList());
  }
```

Let's add the new method in the product quantity modifier component:

```java
  void addProductQuantityBasedOnCancelledOrder(List<OrderDetails> orderDetailsList) {
    for (OrderDetails orderDetails : orderDetailsList) {
      Product product = productService.fetchProductById(orderDetails.getProductId()).get();
      product.addProductQuantity(orderDetails.getQuantity());
    }
  }
```

and the method in our `Product` class:

```java
  public void addProductQuantity(int amountToAdd) {
    this.productQuantity = this.productQuantity + amountToAdd;
  }
```

Lastly, let's add the new method in our controller:

```java
  @DeleteMapping("/api/v1/order/{orderId}")
  public void cancelOrder(@PathVariable long orderId) {
    Order order = orderService.fetchCheckedOutOrderById(orderId).get();
    orderService.cancelOrder(order);
  }
```

Run the application and test the changes.

Let's make the response code of our delete endpoint more specific.

```java
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/api/v1/order/{orderId}")
  public void cancelOrder(@PathVariable long orderId) {
    //other code
  }
```

Run the application and test the changes.

## Error Handling for Web Service

### Errors in Product endpoints

If we try the `GET` endpoint using an ID that doesn't exist, the response shows Internal Server error and the logs show exceptions. Let's fix that here.

First, let's make the error/exception more specific. Under the `web` package, create a new package called `apierror`, then add a new class called `ResourceNotFoundException`:

```java
public class ResourceNotFoundException extends RuntimeException {}
```

Then, let's use this new exception in our `ProductController`:

```java
    Product product =
        productService.fetchProductById(id).orElseThrow(ResourceNotFoundException::new);
```
Here we changed the logic so that if there is there is no fetched product for the ID, it will throw a `ResourceNotFoundException`.

Let's run the application.

The response of the endpoint is still the same and there is still an exception in the logs but it's now referring to `ResourceNotFoundException`.

Now, let's add the actual handling for the `ResourceNotFoundException`. Add a new class named `GlobalExceptionHandler` under the `apierrorr` package.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({ResourceNotFoundException.class})
  public void handleResourceNotFoundException() {}
}
```

The `@RestControllerAdvice` annotation allows us to handle exceptions across the whole application.

The `@ExceptionHandler` annotation is assigned to a method which would control how to handle the specified exception.

Run the application and test the endpoint again.

The response code is now `404 NOT FOUND`. There's also no more exception in the logs.

To give more details on the error, let's add a response body.

Under the `apierror` package, add a new class called `ApiErrorResponse`.

```java
class ApiErrorResponse {
  @Getter private final String errorCode;
  @Getter private final String errorMessage;

  public ApiErrorResponse(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }
}
```

Update the handler in our `GlobalExceptionHandler` class:

```java
  public ApiErrorResponse handleResourceNotFoundException() {
    return new ApiErrorResponse("RESOURCE_NOT_FOUND", "The target resource does not exist");
  }
```

Run the application and test the changes. We now see that there is a response body for our error.


### Errors in Order endpoints

Apply the same error handling to our cancel order endpoint:

```java
    Order order =
        orderService.fetchCheckedOutOrderById(orderId).orElseThrow(ResourceNotFoundException::new);
```

Now, let's add the logic for our business rule. Orders can only be cancelled within 3 days after it was ordered.

In Order class, add the method for the logic:

```java
  boolean canBeCancelledBasedOnOrderDate() {
    return getDateOrdered()
        .toLocalDate()
        .isAfter(LocalDate.now().minusDays(MAXIMUM_NUMBER_OF_DAYS_FOR_CANCELLATION));
  }
```

And in the order service:
```java
  void cancelOrder(Order order) throws UnableToCancelOrderException {
    if (order.canBeCancelledBasedOnOrderDate()) {
      order.cancel();
      orderProductQuantityModifier.addProductQuantityBasedOnCancelledOrder(
          order.getOrderDetailsList());
    } else {
      throw new UnableToCancelOrderException(
          "Can't cancel order that was ordered more than 3 days ago");
    }
  }
```

Add the new exception under the `order` package:
```java
public class UnableToCancelOrderException extends RuntimeException {
  public UnableToCancelOrderException(String errorMessage) {
    super(errorMessage);
  }
}
```

To test the changes, let's temporarily alter the date ordered field in the code.

Run the application and test changes. You should see the exception in the logs.

Let's handle this new error. First, our exception is very specific. It would be better to create a more generic exception and add that in our global handling instead.

Add the more generic exception that would be used in our api response and error handling. Call this class `InvalidOperationException` and add it under the `apierror` package.

```java
public class InvalidOperationException extends RuntimeException {

  @Getter(AccessLevel.PACKAGE)
  private final String errorCode;

  @Getter(AccessLevel.PACKAGE)
  private final String errorMessage;

  public InvalidOperationException(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }
}
```

Then add the new exception in the controller logic:
```java
    try {
      orderService.cancelOrder(order);
    } catch (UnableToCancelOrderException e) {
      throw new InvalidOperationException(
          "UNABLE_TO_CANCEL_ORDER", "Order is not allowed to be cancelled");
    }
```

Lastly, add the new exception in our global exception handler:
```java
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(InvalidOperationException.class)
  public ApiErrorResponse handleInvalidOperationException(InvalidOperationException e) {
    return new ApiErrorResponse(e.getErrorCode(), e.getErrorMessage());
  }
```
Notice that there's an argument for the method. We can set the exception as an argument and be able to use the object if needed.

Run the application and test the changes.

Now we'll add our last validation for the order endpoint. We should not allow order creation if there is not enough product quantity.

To do this, let's add the logic first. Update the deduct method in `OrderProductQuantityModifier`.

```java
  void deductProductQuantityBasedOnOrder(List<OrderDetails> orderDetailsList) {
    Map<Product, Integer> amountToDeductPerProductMap = new HashMap<>();
    for (OrderDetails orderDetails : orderDetailsList) {
      Product product = productService.fetchProductById(orderDetails.getProductId()).get();
      amountToDeductPerProductMap.merge(product, orderDetails.getQuantity(), Integer::sum);
    }

    amountToDeductPerProductMap.forEach(
        (product, totalAmountToDeduct) -> {
          if (totalAmountToDeduct > product.getProductQuantity()) {
            throw new NotEnoughProductsException("Not enough product quantity for the order");
          }
        });
    amountToDeductPerProductMap.forEach(Product::deductProductQuantity);
  }
```

Add the new exception under the `order` package.
```java
class NotEnoughProductsException extends RuntimeException {
  NotEnoughProductsException(String errorMessage) {
    super(errorMessage);
  }
}
```

Because of our new logic in the deduct method, it's important to add hash code logic for the product class:
```java
@EqualsAndHashCode(of = "id")
public class Product {
  //other code
}
```

Run the application and test the logic.

Finally, let's add api handling for this error.

Update the controller to catch the `NotEnoughProductsException`.
```java
  public OrderResponse createOrder(@RequestBody OrderRequest orderRequest) {
    try {
      Order order = orderService.createNewOrder(orderRequest.getOrderDetails());
      return new OrderResponse(order);
    } catch (NotEnoughProductsException e) {
      throw new InvalidOperationException(
          "NOT_ENOUGH_PRODUCTS", "There is not enought product quantity for the request order");
    }
  }
```
