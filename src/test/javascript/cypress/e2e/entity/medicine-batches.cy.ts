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

describe('MedicineBatches e2e test', () => {
  const medicineBatchesPageUrl = '/medicine-batches';
  const medicineBatchesPageUrlPattern = new RegExp('/medicine-batches(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const medicineBatchesSample = {};

  let medicineBatches;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/medicine-batches+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/medicine-batches').as('postEntityRequest');
    cy.intercept('DELETE', '/api/medicine-batches/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (medicineBatches) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/medicine-batches/${medicineBatches.id}`,
      }).then(() => {
        medicineBatches = undefined;
      });
    }
  });

  it('MedicineBatches menu should load MedicineBatches page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('medicine-batches');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('MedicineBatches').should('exist');
    cy.url().should('match', medicineBatchesPageUrlPattern);
  });

  describe('MedicineBatches page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(medicineBatchesPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create MedicineBatches page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/medicine-batches/new$'));
        cy.getEntityCreateUpdateHeading('MedicineBatches');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', medicineBatchesPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/medicine-batches',
          body: medicineBatchesSample,
        }).then(({ body }) => {
          medicineBatches = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/medicine-batches+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [medicineBatches],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(medicineBatchesPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details MedicineBatches page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('medicineBatches');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', medicineBatchesPageUrlPattern);
      });

      it('edit button click should load edit MedicineBatches page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('MedicineBatches');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', medicineBatchesPageUrlPattern);
      });

      it('edit button click should load edit MedicineBatches page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('MedicineBatches');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', medicineBatchesPageUrlPattern);
      });

      it('last delete button click should delete instance of MedicineBatches', () => {
        cy.intercept('GET', '/api/medicine-batches/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('medicineBatches').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', medicineBatchesPageUrlPattern);

        medicineBatches = undefined;
      });
    });
  });

  describe('new MedicineBatches page', () => {
    beforeEach(() => {
      cy.visit(`${medicineBatchesPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('MedicineBatches');
    });

    it('should create an instance of MedicineBatches', () => {
      cy.get(`[data-cy="batchNumber"]`).type('inculcate');
      cy.get(`[data-cy="batchNumber"]`).should('have.value', 'inculcate');

      cy.get(`[data-cy="expiryDate"]`).type('2025-12-02');
      cy.get(`[data-cy="expiryDate"]`).blur();
      cy.get(`[data-cy="expiryDate"]`).should('have.value', '2025-12-02');

      cy.get(`[data-cy="quantity"]`).type('24378');
      cy.get(`[data-cy="quantity"]`).should('have.value', '24378');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        medicineBatches = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', medicineBatchesPageUrlPattern);
    });
  });
});
