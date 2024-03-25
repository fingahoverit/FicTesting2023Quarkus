package com.cgi.fic.tests.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import com.cgi.fic.tests.service.ProductInBasketService;
import com.cgi.fic.tests.web.rest.errors.BadRequestAlertException;
import com.cgi.fic.tests.web.util.HeaderUtil;
import com.cgi.fic.tests.web.util.ResponseUtil;
import com.cgi.fic.tests.service.dto.ProductInBasketDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.cgi.fic.tests.domain.ProductInBasket}.
 */
@Path("/api/product-in-baskets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ProductInBasketResource {

    private final Logger log = LoggerFactory.getLogger(ProductInBasketResource.class);

    private static final String ENTITY_NAME = "productInBasket";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    ProductInBasketService productInBasketService;
    /**
     * {@code POST  /product-in-baskets} : Create a new productInBasket.
     *
     * @param productInBasketDTO the productInBasketDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new productInBasketDTO, or with status {@code 400 (Bad Request)} if the productInBasket has already an ID.
     */
    @POST
    public Response createProductInBasket(@Valid ProductInBasketDTO productInBasketDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save ProductInBasket : {}", productInBasketDTO);
        if (productInBasketDTO.id != null) {
            throw new BadRequestAlertException("A new productInBasket cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = productInBasketService.addProductToBasket(productInBasketDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /product-in-baskets} : Updates an existing productInBasket.
     *
     * @param productInBasketDTO the productInBasketDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated productInBasketDTO,
     * or with status {@code 400 (Bad Request)} if the productInBasketDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the productInBasketDTO couldn't be updated.
     */
    @PUT
    public Response updateProductInBasket(@Valid ProductInBasketDTO productInBasketDTO) {
        log.debug("REST request to update ProductInBasket : {}", productInBasketDTO);
        if (productInBasketDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        var result = productInBasketService.persistOrUpdate(productInBasketDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, productInBasketDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /product-in-baskets/:id} : delete the "id" productInBasket.
     *
     * @param id the id of the productInBasketDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteProductInBasket(@PathParam("id") Long id) {
        log.debug("REST request to delete ProductInBasket : {}", id);
        productInBasketService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /product-in-baskets} : get all the productInBaskets.
     *     * @return the {@link Response} with status {@code 200 (OK)} and the list of productInBaskets in body.
     */
    @GET
    public List<ProductInBasketDTO> getAllProductInBaskets() {
        log.debug("REST request to get all ProductInBaskets");
        return productInBasketService.findAll();
    }


    /**
     * {@code GET  /product-in-baskets/:id} : get the "id" productInBasket.
     *
     * @param id the id of the productInBasketDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the productInBasketDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getProductInBasket(@PathParam("id") Long id) {
        log.debug("REST request to get ProductInBasket : {}", id);
        Optional<ProductInBasketDTO> productInBasketDTO = productInBasketService.findOne(id);
        return ResponseUtil.wrapOrNotFound(productInBasketDTO);
    }
}
