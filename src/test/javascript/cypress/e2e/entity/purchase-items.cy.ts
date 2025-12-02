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

describe('PurchaseItems e2e test', () => {
  const purchaseItemsPageUrl = '/purchase-items';
  const purchaseItemsPageUrlPattern = new RegExp('/purchase-items(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const purchaseItemsSample = {};

  let purchaseItems;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/purchase-items+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/purchase-items').as('postEntityRequest');
    cy.intercept('DELETE', '/api/purchase-items/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (purchaseItems) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/purchase-items/${purchaseItems.id}`,
      }).then(() => {
        purchaseItems = undefined;
      });
    }
  });

  it('PurchaseItems menu should load PurchaseItems page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('purchase-items');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('PurchaseItems').should('exist');
    cy.url().should('match', purchaseItemsPageUrlPattern);
  });

  describe('PurchaseItems page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(purchaseItemsPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create PurchaseItems page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/purchase-items/new$'));
        cy.getEntityCreateUpdateHeading('PurchaseItems');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', purchaseItemsPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/purchase-items',
          body: purchaseItemsSample,
        }).then(({ body }) => {
          purchaseItems = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/purchase-items+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [purchaseItems],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(purchaseItemsPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details PurchaseItems page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('purchaseItems');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', purchaseItemsPageUrlPattern);
      });

      it('edit button click should load edit PurchaseItems page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('PurchaseItems');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', purchaseItemsPageUrlPattern);
      });

      it('edit button click should load edit PurchaseItems page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('PurchaseItems');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', purchaseItemsPageUrlPattern);
      });

      it('last delete button click should delete instance of PurchaseItems', () => {
        cy.intercept('GET', '/api/purchase-items/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('purchaseItems').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', purchaseItemsPageUrlPattern);

        purchaseItems = undefined;
      });
    });
  });

  describe('new PurchaseItems page', () => {
    beforeEach(() => {
      cy.visit(`${purchaseItemsPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('PurchaseItems');
    });

    it('should create an instance of PurchaseItems', () => {
      cy.get(`[data-cy="quantity"]`).type('1772');
      cy.get(`[data-cy="quantity"]`).should('have.value', '1772');

      cy.get(`[data-cy="price"]`).type('2504.23');
      cy.get(`[data-cy="price"]`).should('have.value', '2504.23');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        purchaseItems = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', purchaseItemsPageUrlPattern);
    });
  });
});
