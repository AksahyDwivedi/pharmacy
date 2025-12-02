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

describe('Sales e2e test', () => {
  const salesPageUrl = '/sales';
  const salesPageUrlPattern = new RegExp('/sales(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const salesSample = {};

  let sales;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/sales+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/sales').as('postEntityRequest');
    cy.intercept('DELETE', '/api/sales/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (sales) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/sales/${sales.id}`,
      }).then(() => {
        sales = undefined;
      });
    }
  });

  it('Sales menu should load Sales page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('sales');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Sales').should('exist');
    cy.url().should('match', salesPageUrlPattern);
  });

  describe('Sales page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(salesPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Sales page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/sales/new$'));
        cy.getEntityCreateUpdateHeading('Sales');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', salesPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/sales',
          body: salesSample,
        }).then(({ body }) => {
          sales = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/sales+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [sales],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(salesPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Sales page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('sales');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', salesPageUrlPattern);
      });

      it('edit button click should load edit Sales page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Sales');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', salesPageUrlPattern);
      });

      it('edit button click should load edit Sales page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Sales');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', salesPageUrlPattern);
      });

      it('last delete button click should delete instance of Sales', () => {
        cy.intercept('GET', '/api/sales/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('sales').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', salesPageUrlPattern);

        sales = undefined;
      });
    });
  });

  describe('new Sales page', () => {
    beforeEach(() => {
      cy.visit(`${salesPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Sales');
    });

    it('should create an instance of Sales', () => {
      cy.get(`[data-cy="saleDate"]`).type('2025-12-02T04:37');
      cy.get(`[data-cy="saleDate"]`).blur();
      cy.get(`[data-cy="saleDate"]`).should('have.value', '2025-12-02T04:37');

      cy.get(`[data-cy="invoiceNumber"]`).type('provided');
      cy.get(`[data-cy="invoiceNumber"]`).should('have.value', 'provided');

      cy.get(`[data-cy="totalAmount"]`).type('21221.62');
      cy.get(`[data-cy="totalAmount"]`).should('have.value', '21221.62');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        sales = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', salesPageUrlPattern);
    });
  });
});
