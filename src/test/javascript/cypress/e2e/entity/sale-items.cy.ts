import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('SaleItems e2e test', () => {
  const saleItemsPageUrl = '/sale-items';
  const saleItemsPageUrlPattern = new RegExp('/sale-items(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const saleItemsSample = {};

  let saleItems;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/sale-items+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/sale-items').as('postEntityRequest');
    cy.intercept('DELETE', '/api/sale-items/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (saleItems) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/sale-items/${saleItems.id}`,
      }).then(() => {
        saleItems = undefined;
      });
    }
  });

  it('SaleItems menu should load SaleItems page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('sale-items');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('SaleItems').should('exist');
    cy.url().should('match', saleItemsPageUrlPattern);
  });

  describe('SaleItems page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(saleItemsPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create SaleItems page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/sale-items/new$'));
        cy.getEntityCreateUpdateHeading('SaleItems');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', saleItemsPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/sale-items',
          body: saleItemsSample,
        }).then(({ body }) => {
          saleItems = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/sale-items+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [saleItems],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(saleItemsPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details SaleItems page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('saleItems');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', saleItemsPageUrlPattern);
      });

      it('edit button click should load edit SaleItems page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('SaleItems');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', saleItemsPageUrlPattern);
      });

      it('edit button click should load edit SaleItems page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('SaleItems');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', saleItemsPageUrlPattern);
      });

      it('last delete button click should delete instance of SaleItems', () => {
        cy.intercept('GET', '/api/sale-items/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('saleItems').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', saleItemsPageUrlPattern);

        saleItems = undefined;
      });
    });
  });

  describe('new SaleItems page', () => {
    beforeEach(() => {
      cy.visit(`${saleItemsPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('SaleItems');
    });

    it('should create an instance of SaleItems', () => {
      cy.get(`[data-cy="quantity"]`).type('31194');
      cy.get(`[data-cy="quantity"]`).should('have.value', '31194');

      cy.get(`[data-cy="price"]`).type('17800.89');
      cy.get(`[data-cy="price"]`).should('have.value', '17800.89');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        saleItems = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', saleItemsPageUrlPattern);
    });
  });
});
