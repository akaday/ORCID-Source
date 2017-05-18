<#--

    =============================================================================

    ORCID (R) Open Source
    http://orcid.org

    Copyright (c) 2012-2014 ORCID, Inc.
    Licensed under an MIT-Style License (MIT)
    http://orcid.org/open-source-license

    This copyright and license information (including a link to the full license)
    shall be included in its entirety in all copies or substantial portion of
    the software.

    =============================================================================

-->
<@public classes=['home'] >
<div ng-controller="SearchCtrl" id="SearchCtrl">
	<div class="row">
		<div class="col-md-offset-3 col-md-9 col-sm-offset-3 col-sm-offset-9 col-xs-12">
			<div class="main-search">
				<div class="row">
					<h1>${springMacroRequestContext.getMessage("orcid_bio_search.h1advancedsearch")}</h1>
					<p>${springMacroRequestContext.getMessage("orcid_bio_search.searchpublicly")}</p>
				</div>
				<form id="searchForm" class="form-horizontal" ng-submit="getFirstResults()">
					<fieldset>
						<div class="row">
							<div class="control-group col-md-6">
								<!-- Given name -->
								<label for="givenName" class="control-label">${springMacroRequestContext.getMessage("orcid_bio_search.labelfirstname")}</label>
								<div class="controls">
									<input type="text" class="input-xlarge" name="givenNames" id="givenNames" ng-model="input.givenNames">
								</div>
							</div>
							<div class="control-group col-md-6">
								<!-- Family name -->
								<label for="familyName" class="control-label">${springMacroRequestContext.getMessage("orcid_bio_search.labellastname")}</label>
								<div class="controls">
									<input type="text" class="input-xlarge" name="familyName" id="familyName" ng-model="input.familyName">
								</div>
							</div>
						</div>
						<div class="row">
							<div class="control-group">
								<div class="checkbox">
									<!-- Other names -->
									<label for="otherNamesSearchable">
									<input type="checkbox" name="otherNamesSearchable" id="otherNamesSearchable" ng-model="input.searchOtherNames">
									${springMacroRequestContext.getMessage("orcid_bio_search.labelalsosearchothernames")}</label>
								</div>
							</div>
						</div>
						<div class="row">
							<div class="control-group col-md-6">
								<!-- Affiliation organization -->
								<label for="affiliationOrgName" class="control-label">${springMacroRequestContext.getMessage("orcid_bio_search.labelaffiliationorgname")}</label>
								<div class="popover-help-container">
						            <a href="javascript:void(0);"><i class="glyphicon glyphicon-question-sign"></i></a>
						            <div id="search-help-affiliation" class="popover bottom">
						              <div class="arrow"></div>
						              <div class="popover-content">
						                <p>${springMacroRequestContext.getMessage("orcid_bio_search.popover_help.affiliation")}</p>
						              </div>
						            </div>
						         </div>
								<div class="controls">
									<input type="text" class="input-xlarge" name="affiliationOrgName" id="affiliationOrgName" ng-model="input.affiliationOrgName">
								</div>
							</div>
							<div class="control-group col-md-6">
								<!-- Keyword -->
								<label for="familyName" class="control-label">${springMacroRequestContext.getMessage("orcid_bio_search.labelkeywords")}</label>
								<div class="popover-help-container">
						            <a href="javascript:void(0);"><i class="glyphicon glyphicon-question-sign"></i></a>
						            <div id="search-help-keyword" class="popover bottom">
						              <div class="arrow"></div>
						              <div class="popover-content">
						                <p>${springMacroRequestContext.getMessage("orcid_bio_search.popover_help.keyword")}</p>
						              </div>
						            </div>
						         </div>
								<div class="controls">
										<input type="text" class="input-xlarge" name="keyword" id="keyword" ng-model="input.keyword">
								</div>
							</div>
						</div>
					</fieldset>
					<hr>
					<fieldset>
						<div class="row">
							<p>${springMacroRequestContext.getMessage("orcid_bio_search.searchorcid")}</p>
							<div class="control-group">
								<!-- Search by ORCID iD -->
								<label for="orcid" class="control-label">${springMacroRequestContext.getMessage("orcid_bio_search.labelsearchbyorcid")}</label>
								<div class="controls">
									<input type="text" class="input-xlarge" name="orcid" id="orcid" ng-model="input.text">
									<span id="invalid-orcid" class="orcid-error"ng-cloak ng-hide="isValidOrcidId()"><@orcid.msg 'admin.profile_deprecation.errors.invalid_regex' /></span>
								</div>
							</div>
						</div>
					</fieldset>
					<hr>
					<div class="row">
						<p ng-if="hasErrors" ng-cloak><span class="orcid-error">${springMacroRequestContext.getMessage("orcid_bio_search.pyoumustpopulate")}</span></p>
						<div class="control-group">
							<div class="controls">
								<button class="btn btn-primary" type="submit">${springMacroRequestContext.getMessage("orcid_bio_search.btnsearch")}</button>
								<span id="ajax-loader-search" class="orcid-hide"><i class="glyphicon glyphicon-refresh spin x2 green"></i></span>
							</div>
						</div>
					</div>
				</form>
			</div>
		</div>
	</div>
	<div class="row search-results">
		<div class="col-md-12">
			<#if noResultsFound??>
				<!-- no results -->
			<#else>
				<#include "includes/search/search_results.ftl"/>
			</#if>
		</div>
	</div>
</div>
</@public>