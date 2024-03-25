package com.cgi.fic.tests.service;

import com.cgi.fic.tests.domain.ProductInBasket;
import com.cgi.fic.tests.service.dto.ProductInBasketDTO;
import com.cgi.fic.tests.service.mapper.ProductInBasketMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ProductInBasketService {

    private final Logger log = LoggerFactory.getLogger(ProductInBasketService.class);

    @Inject
    ProductInBasketMapper productInBasketMapper;

    @Transactional
    public ProductInBasketDTO addProductToBasket(ProductInBasketDTO productInBasketDTO) {
        log.debug("Request to Add product to ProductInBasket : {}", productInBasketDTO);

        List<ProductInBasketDTO> pibList = findAllProductsInBasket(productInBasketDTO.basketId);

        for (ProductInBasketDTO pib : pibList) {
            if (pib.productId == productInBasketDTO.productId) {
                productInBasketDTO.id = pib.id;
                productInBasketDTO.quantity += 1;
                break;
            }
        }

        var productInBasket = productInBasketMapper.toEntity(productInBasketDTO);
        productInBasket = ProductInBasket.persistOrUpdate(productInBasket);
        return productInBasketMapper.toDto(productInBasket);
    }

    @Transactional
    public ProductInBasketDTO persistOrUpdate(ProductInBasketDTO productInBasketDTO) {
        log.debug("Request to save ProductInBasket : {}", productInBasketDTO);

        var productInBasket = productInBasketMapper.toEntity(productInBasketDTO);
        productInBasket = ProductInBasket.persistOrUpdate(productInBasket);
        return productInBasketMapper.toDto(productInBasket);
    }

    /**
     * Delete the ProductInBasket by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete ProductInBasket : {}", id);
        ProductInBasket.findByIdOptional(id).ifPresent(productInBasket -> {
            productInBasket.delete();
        });
    }

    /**
     * Get one productInBasket by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<ProductInBasketDTO> findOne(Long id) {
        log.debug("Request to get ProductInBasket : {}", id);
        return ProductInBasket.findByIdOptional(id)
                .map(productInBasket -> productInBasketMapper.toDto((ProductInBasket) productInBasket));
    }

    /**
     * Get all the productInBaskets.
     * 
     * @return the list of entities.
     */
    public List<ProductInBasketDTO> findAllProductsInBasket(Long id) {
        log.debug("Request to get all ProductInBaskets of a basket");
        List<ProductInBasket> productInBaskets = ProductInBasket.find("basket_id", id).list();
        return productInBasketMapper.toDto(productInBaskets);
    }

    /**
     * Get all the productInBaskets.
     * 
     * @return the list of entities.
     */
    public List<ProductInBasketDTO> findAll() {
        log.debug("Request to get all ProductInBaskets");
        List<ProductInBasket> productInBaskets = ProductInBasket.findAll().list();
        return productInBasketMapper.toDto(productInBaskets);
    }

}
