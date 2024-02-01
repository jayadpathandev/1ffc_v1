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
import $ from 'jquery';

interface Response {
    [key:string]: string;
}

class UseCase {
    private id      : string = '';
    private state   : string = 'idle';
    private data    : Map<string, string> = new Map<string, string>();
    private waiting : Map<number, () => void> = new Map<number, () => void>();
    private nextId  : number = 1;

    constructor(
                usecase : string
            ) {
        this.id    = usecase;
        this.state = 'running';

        $.ajax("get_i18n", {
            method: 'post',
            data:   {uc: usecase},
        }).done((data:Response) => {
            const prefix = usecase + "_";
            for(const key in data) {
                if (key.startsWith(prefix) === true) {
                    const value = data[key];
                    const short = key.substring(prefix.length);
                    this.data.set(short, value);
                } else {
                    console.error("Invalid key found for use case [" + usecase + "] => " + key);
                }
            }
            this.state ='success';
        }).always(() => {
            if (this.state !== 'success') {
                this.state = 'error';
            }
            this.waiting.forEach((callback:()=>void,  id: number) => {
                callback();
            });
            this.waiting.clear();
        });
    }

    public get(
            id       : string,
            callback : (text:string) => void
        ) : ()=>void {

        let retVal = ()=>{};

        if (this.state === 'success') {
            const text = this.data.get(id);

            if (text !== undefined) {
                callback(text);
            } else {
                throw('18N ID [' + id + '] was not defined.');
            }
        } else if (this.state === 'error') {
            throw('I18N for use case [' + this.id + '] could not be loaded.');
        } else if (this.state === 'running') {
            let regId = this.nextId;
            this.waiting.set(regId, () => this.get(id, callback));
            retVal = ()=>{this.waiting.delete(regId);}
            this.nextId += 1;
        }
        return retVal;
    }

    public get_many(
                ids       : string[],
                callback : (data:Map<string, string>) => void
        ) : ()=>void {

    let retVal = ()=>{};

    if (this.state === 'success') {
        let retval = new Map<string, string>();

        ids.forEach((id:string) => {
            const text = this.data.get(id);
            if (text === undefined) {
                throw('18N ID [' + id + '] was not defined.');
            }
            retval.set(id, text);
        });
        callback(retval);
    } else if (this.state === 'error') {
        throw('I18N for use case [' + this.id + '] could not be loaded.');
    } else if (this.state === 'running') {
        let regId = this.nextId;
        this.waiting.set(regId, () => this.get_many(ids, callback));
        retVal = ()=>{this.waiting.delete(regId);}
        this.nextId += 1;
    }
    return retVal;
}
}

export class I18nService {
    private static cache : Map<string, UseCase> = new Map<string, UseCase>();

    private usecase : UseCase;

    constructor(
                usecase : string
            ) {
        let target = I18nService.cache.get(usecase);

        if (target === undefined) {
            target = new UseCase(usecase);
            I18nService.cache.set(usecase, target);
        }

        this.usecase = target;
    }

    public get(
                id       : string,
                callback : (text:string) => void
            ) :  ()=>void {
        return this.usecase.get(id, callback);
    }

    public get_many(
                ids : string[],
                callback : (text:Map<string, string>) => void
            ) {
        this.usecase.get_many(ids, callback);
    }
}