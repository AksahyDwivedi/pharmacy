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

describe('Purchases e2e test', () => {
  const purchasesPageUrl = '/purchases';
  const purchasesPageUrlPattern = new RegExp('/purchases(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const purchasesSample = {};

  let purchases;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/purchases+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/purchases').as('postEntityRequest');
    cy.intercept('DELETE', '/api/purchases/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (purchases) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/purchases/${purchases.id}`,
      }).then(() => {
        purchases = undefined;
      });
    }
  });

  it('Purchases menu should load Purchases page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('purchases');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Purchases').should('exist');
    cy.url().should('match', purchasesPageUrlPattern);
  });

  describe('Purchases page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(purchasesPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Purchases page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/purchases/new$'));
        cy.getEntityCreateUpdateHeading('Purchases');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', purchasesPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/purchases',
          body: purchasesSample,
        }).then(({ body }) => {
          purchases = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/purchases+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [purchases],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(purchasesPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Purchases page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('purchases');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', purchasesPageUrlPattern);
      });

      it('edit button click should load edit Purchases page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Purchases');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', purchasesPageUrlPattern);
      });

      it('edit button click should load edit Purchases page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Purchases');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', purchasesPageUrlPattern);
      });

      it('last delete button click should delete instance of Purchases', () => {
        cy.intercept('GET', '/api/purchases/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('purchases').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', purchasesPageUrlPattern);

        purchases = undefined;
      });
    });
  });

  describe('new Purchases page', () => {
    beforeEach(() => {
      cy.visit(`${purchasesPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Purchases');
    });

    it('should create an instance of Purchases', () => {
      cy.get(`[data-cy="purchaseDate"]`).type('2025-12-01');
      cy.get(`[data-cy="purchaseDate"]`).blur();
      cy.get(`[data-cy="purchaseDate"]`).should('have.value', '2025-12-01');

      cy.get(`[data-cy="invoiceNumber"]`).type('as bob');
      cy.get(`[data-cy="invoiceNumber"]`).should('have.value', 'as bob');

      cy.get(`[data-cy="totalAmount"]`).type('30906.79');
      cy.get(`[data-cy="totalAmount"]`).should('have.value', '30906.79');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        purchases = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', purchasesPageUrlPattern);
    });
  });
});
