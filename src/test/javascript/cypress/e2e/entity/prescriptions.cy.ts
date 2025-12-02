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

describe('Prescriptions e2e test', () => {
  const prescriptionsPageUrl = '/prescriptions';
  const prescriptionsPageUrlPattern = new RegExp('/prescriptions(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const prescriptionsSample = {};

  let prescriptions;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/prescriptions+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/prescriptions').as('postEntityRequest');
    cy.intercept('DELETE', '/api/prescriptions/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (prescriptions) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/prescriptions/${prescriptions.id}`,
      }).then(() => {
        prescriptions = undefined;
      });
    }
  });

  it('Prescriptions menu should load Prescriptions page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('prescriptions');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Prescriptions').should('exist');
    cy.url().should('match', prescriptionsPageUrlPattern);
  });

  describe('Prescriptions page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(prescriptionsPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Prescriptions page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/prescriptions/new$'));
        cy.getEntityCreateUpdateHeading('Prescriptions');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', prescriptionsPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/prescriptions',
          body: prescriptionsSample,
        }).then(({ body }) => {
          prescriptions = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/prescriptions+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [prescriptions],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(prescriptionsPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Prescriptions page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('prescriptions');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', prescriptionsPageUrlPattern);
      });

      it('edit button click should load edit Prescriptions page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Prescriptions');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', prescriptionsPageUrlPattern);
      });

      it('edit button click should load edit Prescriptions page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Prescriptions');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', prescriptionsPageUrlPattern);
      });

      it('last delete button click should delete instance of Prescriptions', () => {
        cy.intercept('GET', '/api/prescriptions/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('prescriptions').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', prescriptionsPageUrlPattern);

        prescriptions = undefined;
      });
    });
  });

  describe('new Prescriptions page', () => {
    beforeEach(() => {
      cy.visit(`${prescriptionsPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Prescriptions');
    });

    it('should create an instance of Prescriptions', () => {
      cy.get(`[data-cy="doctorName"]`).type('harmful but playfully');
      cy.get(`[data-cy="doctorName"]`).should('have.value', 'harmful but playfully');

      cy.get(`[data-cy="prescriptionDate"]`).type('2025-12-01');
      cy.get(`[data-cy="prescriptionDate"]`).blur();
      cy.get(`[data-cy="prescriptionDate"]`).should('have.value', '2025-12-01');

      cy.get(`[data-cy="notes"]`).type('../fake-data/blob/hipster.txt');
      cy.get(`[data-cy="notes"]`).invoke('val').should('match', new RegExp('../fake-data/blob/hipster.txt'));

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        prescriptions = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', prescriptionsPageUrlPattern);
    });
  });
});
