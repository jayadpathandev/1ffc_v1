// (c) Copyright 2021 Sorriso Technologies, Inc(r), All Rights Reserved,
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
import React    from 'react';
import ReactDOM from 'react-dom';

import { ArchiveIcon      } from '@primer/octicons-react';
import { CalendarIcon     } from '@primer/octicons-react';
import { ChevronDownIcon  } from '@primer/octicons-react';
import { ChevronLeftIcon  } from '@primer/octicons-react';
import { ChevronRightIcon } from '@primer/octicons-react';
import { ChevronUpIcon    } from '@primer/octicons-react';
import { DownloadIcon     } from '@primer/octicons-react';
import { InfoIcon         } from '@primer/octicons-react';
import { TrashIcon        } from '@primer/octicons-react';
import { TriangleDownIcon } from '@primer/octicons-react';
import { TriangleLeftIcon } from '@primer/octicons-react';
import { TriangleUpIcon   } from '@primer/octicons-react';

export interface IconProps {
    size: number;
}

export function IconArchive(props : IconProps) {
    return <ArchiveIcon size={props.size}/>
}

export function IconCalendar(props : IconProps) {
    return <CalendarIcon size={props.size}/>
}

export function IconChevronDown(props : IconProps) {
    return <ChevronDownIcon size={props.size}/>
}

export function IconChevronLeft(props : IconProps) {
    return <ChevronLeftIcon size={props.size}/>
}

export function IconChevronRight(props : IconProps) {
    return <ChevronRightIcon size={props.size}/>
}

export function IconChevronUp(props : IconProps) {
    return <ChevronUpIcon size={props.size}/>
}

export function IconDownload(props : IconProps) {
    return <DownloadIcon size={props.size}/>
}

export function IconInfo(props : IconProps) {
    return <InfoIcon size={props.size}/>
}

export function IconTrash(props : IconProps) {
    return <TrashIcon size={props.size}/>
}

export function IconTriangleDown(props : IconProps) {
    return <TriangleDownIcon size={props.size}/>
}

export function IconTriangleLeft(props : IconProps) {
    return <TriangleLeftIcon size={props.size}/>
}

export function IconTriangleUp(props : IconProps) {
    return <TriangleUpIcon size={props.size}/>
}
