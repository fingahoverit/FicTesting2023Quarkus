package com.cgi.fic.tests.web.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import com.cgi.fic.tests.TestUtil;
import com.cgi.fic.tests.service.dto.ProductInBasketDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import liquibase.Liquibase;
import io.quarkus.liquibase.LiquibaseFactory;
import org.junit.jupiter.api.*;

import javax.inject.Inject;

import java.util.List;
    
@QuarkusTest
public class ProductInBasketResourceTest {

    private static final TypeRef<ProductInBasketDTO> ENTITY_TYPE = new TypeRef<>() {
    };

    private static final TypeRef<List<ProductInBasketDTO>> LIST_OF_ENTITY_TYPE = new TypeRef<>() {
    };

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;
    private static final Long DEFAULT_PRODUCTID = 1L;

    String adminToken;

    ProductInBasketDTO productInBasketDTO;

    @Inject
    LiquibaseFactory liquibaseFactory;

    @BeforeAll
    static void beforeAllTests() {
        RestAssured.config =
            RestAssured.config().objectMapperConfig(objectMapperConfig().defaultObjectMapper(TestUtil.jsonbObjectMapper()));            
    }

    @BeforeEach
    public void authenticateAdmin() {
        this.adminToken = TestUtil.getAdminToken();
    }

    @BeforeEach
    public void databaseFixture() {
        try (Liquibase liquibase = liquibaseFactory.createLiquibase()) {
            liquibase.dropAll();
            liquibase.validate();
            liquibase.update(liquibaseFactory.createContexts(), liquibaseFactory.createLabels());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductInBasketDTO createEntity() {
        var productInBasketDTO = new ProductInBasketDTO();
        productInBasketDTO.quantity = DEFAULT_QUANTITY;
        productInBasketDTO.productId = DEFAULT_PRODUCTID;
        productInBasketDTO.basketId = 1L;
        return productInBasketDTO;
    }

    @BeforeEach
    public void initTest() {
        productInBasketDTO = createEntity();
    }

    @Test
    public void createProductInBasket() {
        int databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the ProductInBasket
        productInBasketDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(productInBasketDTO)
            .when()
            .post("/api/product-in-baskets")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Validate the ProductInBasket in the database
        var productInBasketDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(productInBasketDTOList).hasSize(databaseSizeBeforeCreate + 1);
        var testProductInBasketDTO = productInBasketDTOList.stream().filter(it -> productInBasketDTO.id.equals(it.id)).findFirst().get();
        assertThat(testProductInBasketDTO.quantity).isEqualTo(DEFAULT_QUANTITY);
    }

    @Test
    public void givenOneEmptyBasketWhenIAddTwoTimesTheSameProductThenBasketShouldContainOnlyOneRowWithAQuantityOfTwo() {
         // Create the first ProductInBasket
         var firstProductInBasketDTO = createEntity();
         firstProductInBasketDTO = given()
         .auth()
         .preemptive()
         .oauth2(adminToken)
         .contentType(APPLICATION_JSON)
         .accept(APPLICATION_JSON)
         .body(firstProductInBasketDTO)
         .when()
         .post("/api/product-in-baskets")
         .then()
         .statusCode(CREATED.getStatusCode())
         .extract().as(ENTITY_TYPE);

         // Create the second ProductInBasket
         var secondProductInBasketDTO = createEntity();
         secondProductInBasketDTO = given()
         .auth()
         .preemptive()
         .oauth2(adminToken)
         .contentType(APPLICATION_JSON)
         .accept(APPLICATION_JSON)
         .body(secondProductInBasketDTO)
         .when()
         .post("/api/product-in-baskets")
         .then()
         .statusCode(CREATED.getStatusCode())
         .extract().as(ENTITY_TYPE);

         var productInBasketDTOList = given()
         .auth()
         .preemptive()
         .oauth2(adminToken)
         .accept(APPLICATION_JSON)
         .when()
         .get("/api/product-in-baskets")
         .then()
         .statusCode(OK.getStatusCode())
         .contentType(APPLICATION_JSON)
         .extract().as(LIST_OF_ENTITY_TYPE);

         assertThat(productInBasketDTOList).hasSize(1);
         var firstProductInBasketList = productInBasketDTOList.stream().filter(it -> it.productId == 1L).findFirst().get();
         assertThat(firstProductInBasketList.quantity).isEqualTo(2);
    }

    @Test
    public void givenOneEmptyBasketWhenIAddTwoDifferentProductsInBasketThenTheBasketsShouldContainTwoRowsForTheTwoProductsWithAQuantityOfOne () {
         // Create the first ProductInBasket
         var firstProductInBasketDTO = createEntity();
         firstProductInBasketDTO = given()
         .auth()
         .preemptive()
         .oauth2(adminToken)
         .contentType(APPLICATION_JSON)
         .accept(APPLICATION_JSON)
         .body(firstProductInBasketDTO)
         .when()
         .post("/api/product-in-baskets")
         .then()
         .statusCode(CREATED.getStatusCode())
         .extract().as(ENTITY_TYPE);

         // Create the second ProductInBasket
         var secondProductInBasketDTO = createEntity();
         secondProductInBasketDTO.productId = 2L;
         secondProductInBasketDTO = given()
         .auth()
         .preemptive()
         .oauth2(adminToken)
         .contentType(APPLICATION_JSON)
         .accept(APPLICATION_JSON)
         .body(secondProductInBasketDTO)
         .when()
         .post("/api/product-in-baskets")
         .then()
         .statusCode(CREATED.getStatusCode())
         .extract().as(ENTITY_TYPE);

         var productInBasketDTOList = given()
         .auth()
         .preemptive()
         .oauth2(adminToken)
         .accept(APPLICATION_JSON)
         .when()
         .get("/api/product-in-baskets")
         .then()
         .statusCode(OK.getStatusCode())
         .contentType(APPLICATION_JSON)
         .extract().as(LIST_OF_ENTITY_TYPE);

         assertThat(productInBasketDTOList).hasSize(2);
         var firstProductInBasketList = productInBasketDTOList.stream().filter(it -> it.productId == 1L).findFirst().get();
         assertThat(firstProductInBasketList.quantity).isEqualTo(1);
         var secondProductInBasketList = productInBasketDTOList.stream().filter(it -> it.productId == 2L).findFirst().get();
         assertThat(secondProductInBasketList.quantity).isEqualTo(1);
    }

    @Test
    public void givenTwoEmptyBasketsWhenIAddTheSameProductToBothBasketsThenTheTwoBasketsShouldContainOnlyOneRowWithAQuantityOfOne() {
         // Create the first ProductInBasket
         var firstProductInBasketDTO = createEntity();
         firstProductInBasketDTO = given()
         .auth()
         .preemptive()
         .oauth2(adminToken)
         .contentType(APPLICATION_JSON)
         .accept(APPLICATION_JSON)
         .body(firstProductInBasketDTO)
         .when()
         .post("/api/product-in-baskets")
         .then()
         .statusCode(CREATED.getStatusCode())
         .extract().as(ENTITY_TYPE);

         // Create the second ProductInBasket
         var secondProductInBasketDTO = createEntity();
         secondProductInBasketDTO.basketId = 2L;
         secondProductInBasketDTO = given()
         .auth()
         .preemptive()
         .oauth2(adminToken)
         .contentType(APPLICATION_JSON)
         .accept(APPLICATION_JSON)
         .body(secondProductInBasketDTO)
         .when()
         .post("/api/product-in-baskets")
         .then()
         .statusCode(CREATED.getStatusCode())
         .extract().as(ENTITY_TYPE);

         var productInBasketDTOList = given()
         .auth()
         .preemptive()
         .oauth2(adminToken)
         .accept(APPLICATION_JSON)
         .when()
         .get("/api/product-in-baskets")
         .then()
         .statusCode(OK.getStatusCode())
         .contentType(APPLICATION_JSON)
         .extract().as(LIST_OF_ENTITY_TYPE);

         System.out.println(productInBasketDTOList);
         assertThat(productInBasketDTOList).hasSize(2);
         var firstProductInBasketList = productInBasketDTOList.stream().filter(it -> it.basketId == 1L).findFirst().get();
         assertThat(firstProductInBasketList.quantity).isEqualTo(1);
         assertThat(firstProductInBasketList.basketId).isEqualTo(1);
         var secondProductInBasketList = productInBasketDTOList.stream().filter(it -> it.basketId == 2L).findFirst().get();
         assertThat(secondProductInBasketList.quantity).isEqualTo(1);
         assertThat(secondProductInBasketList.basketId).isEqualTo(2);
    }

    @Test
    public void createProductInBasketWithExistingId() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the ProductInBasket with an existing ID
        productInBasketDTO.id = 1L;

        // An entity with an existing ID cannot be created, so this API call must fail
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(productInBasketDTO)
            .when()
            .post("/api/product-in-baskets")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the ProductInBasket in the database
        var productInBasketDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(productInBasketDTOList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void checkQuantityIsRequired() throws Exception {
        var databaseSizeBeforeTest = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // set the field null
        productInBasketDTO.quantity = null;

        // Create the ProductInBasket, which fails.
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(productInBasketDTO)
            .when()
            .post("/api/product-in-baskets")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the ProductInBasket in the database
        var productInBasketDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(productInBasketDTOList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void updateProductInBasket() {
        // Initialize the database
        productInBasketDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(productInBasketDTO)
            .when()
            .post("/api/product-in-baskets")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Get the productInBasket
        var updatedProductInBasketDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets/{id}", productInBasketDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().body().as(ENTITY_TYPE);

        // Update the productInBasket
        updatedProductInBasketDTO.quantity = UPDATED_QUANTITY;

        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(updatedProductInBasketDTO)
            .when()
            .put("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode());

        // Validate the ProductInBasket in the database
        var productInBasketDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(productInBasketDTOList).hasSize(databaseSizeBeforeUpdate);
        var testProductInBasketDTO = productInBasketDTOList.stream().filter(it -> updatedProductInBasketDTO.id.equals(it.id)).findFirst().get();
        assertThat(testProductInBasketDTO.quantity).isEqualTo(UPDATED_QUANTITY);
    }

    @Test
    public void updateNonExistingProductInBasket() {
        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(productInBasketDTO)
            .when()
            .put("/api/product-in-baskets")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the ProductInBasket in the database
        var productInBasketDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(productInBasketDTOList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteProductInBasket() {
        // Initialize the database
        productInBasketDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(productInBasketDTO)
            .when()
            .post("/api/product-in-baskets")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeDelete = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Delete the productInBasket
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .delete("/api/product-in-baskets/{id}", productInBasketDTO.id)
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

        // Validate the database contains one less item
        var productInBasketDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(productInBasketDTOList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void getAllProductInBaskets() {
        // Initialize the database
        productInBasketDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(productInBasketDTO)
            .when()
            .post("/api/product-in-baskets")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Get all the productInBasketList
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets?sort=id,desc")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", hasItem(productInBasketDTO.id.intValue()))
            .body("quantity", hasItem(DEFAULT_QUANTITY.intValue()));
    }

    @Test
    public void getProductInBasket() {
        // Initialize the database
        productInBasketDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(productInBasketDTO)
            .when()
            .post("/api/product-in-baskets")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var response = // Get the productInBasket
            given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .accept(APPLICATION_JSON)
                .when()
                .get("/api/product-in-baskets/{id}", productInBasketDTO.id)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ENTITY_TYPE);

        // Get the productInBasket
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets/{id}", productInBasketDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", is(productInBasketDTO.id.intValue()))
            
                .body("quantity", is(DEFAULT_QUANTITY.intValue()));
    }

    @Test
    public void getNonExistingProductInBasket() {
        // Get the productInBasket
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/product-in-baskets/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(NOT_FOUND.getStatusCode());
    }
}
