// (c) Copyright 2022 Sorriso Technologies, Inc(r), All Rights Reserved,
// Patents Pending.
//
// This product is distributed under license from Sorriso Technologies, Inc.
// Use without a proper license is strictly prohibited.  To license this
// software, you may contact Sorriso Technologies at:

// Sorriso Technologies, Inc.
// 400 West Cummings Park
// Suite 1725-184
// Woburn, MA 01801, USA
// +1.978.635.3900

// "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
// Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
// are all Registered Trademarks of Sorriso Technologies, Inc.  "Information Is
// The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
// "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
// "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
// "Persona E-Service", "Persona Customer Intelligence", "Persona Active
// Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
// Technologies, Inc.

//Language + Country where the number 1234.56 is in the format 1.234,56
const european = ["ca-ES","da-DK","de-DE","el-GR","is-IS","it-IT","nl-NL","pt-BR","ro-RO","hr-HR","sq-AL","sv-SE","tr-TR","id-ID","sl-SI","lt-LT","vi-VN","eu-ES","mk-MK","fo-FO","ms-MY","gl-ES","fr-BE","nl-BE","pt-PT","sr-Latn-CS","ms-BN","de-AT","es-ES","sr-Cyrl-CS","de-LU","es-CR","es-VE","es-CO","es-AR","es-EC","es-CL","es-UY","es-PY","es-BO","sr-Cyrl-BA","fy-NL","se-SE","sma-SE","hr-BA","bs-Latn-BA","bs-Cyrl-BA","arn-CL","quz-EC","sr-Latn-BA","smj-SE","quz-BO"];

//Language + Country where the number 1234.56 is in the format 1 234,56
const easternEuropean = ["bg-BG","cs-CZ","fi-FI","fr-FR","hu-HU","nb-NO","pl-PL","ru-RU","sk-SK","uk-UA","be-BY","lv-LV","az-Latn-AZ","ka-GE","uz-Latn-UZ","tt-RU","mn-MN","nn-NO","sv-FI","az-Cyrl-AZ","uz-Cyrl-UZ","fr-CA","fr-LU","fr-MC","sma-NO","smn-FI","se-FI","sms-FI","smj-NO","lb-LU","se-NO"];

//Language + Country where the number 1234.56 is in the format 1'234.56
const swiss = ["de-CH","it-CH","fr-CH","de-LI","rm-CH"];

//Language + Country where the number 1234.56 is in the format 1,234/56
const persian = ["fa-IR"];

//Language + Country where the number 1234.56 is in the format 1 234-56
const kazakhstan = ["kk-KZ","ky-KG"];

//Language + Country where the number 1234.56 is in the format 1 234.56
const estonian = ["et-EE"];

export function parseAmount(
            amount : string,
            locale : string
        ) {
	var clean = amount.replace(/[^0-9.,\-']/g,"");
	if(european.indexOf(locale) > -1) {//check if the number 1234.56 is in the format 1.234,56
		clean = clean.replace(/\./g,"").replace(/,/g,".");
	} else if(easternEuropean.indexOf(locale) > -1) { //check if the number 1234.56 is in the format 1 234,56
		clean = clean.replace(/\./g,"").replace(/,/g,".");
	} else if(swiss.indexOf(locale) > -1) { //check if the number 1234.56 is in the format 1'234.56
		clean = clean.replace(/\'/g,"").replace(/,/g,".")
	} else if(persian.indexOf(locale) > -1) { //check if the number 1234.56 is in the format 1,234/56
		clean = clean.replace(/\,/g,"").replace(/\//g,".")
	} else if(kazakhstan.indexOf(locale) > -1) { //check if the number 1234.56 is in the format 1 234-56
		clean = clean.replace(/-/g,".");
	} else if(estonian.indexOf(locale) > -1) { //check if the number 1234.56 is in the format 1 234.56
		//Do nothing we already removed the space up above
	} else { //assume the number 1234.56 is in the format 1,234.56
		clean = clean.replace(/,/g,"");
	}

	return Number(clean);
}
