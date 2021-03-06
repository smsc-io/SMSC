import {Component} from "@angular/core";
import {Location} from "@angular/common";
import {TranslateService} from "ng2-translate/ng2-translate";
import {Router, ActivatedRoute} from "@angular/router";
import {ColumnDef} from "../model/column-definition";
import {Pagination} from "../model/pagination";
import {CustomersService, REPOSITORY_NAME} from "../customers.service";
import {CustomersViewService} from "./customers-view.service";
import * as clone from "js.clone";
import {NotificationService} from "../../services/notification-service";
import {Customer} from "../model/customer";

@Component({
    selector: 'customers-view',
    templateUrl: './customers-view.component.html',
    styleUrls: ['./customers-view.component.scss'],
    providers: [CustomersViewService]
})

export class CustomersViewComponent {

    public pagination: Pagination = new Pagination(10, null, null, 0);

    public columnDefs: ColumnDef[];

    public rowData = [];

    public selectedRows: ColumnDef[] = [];

    constructor(public translate: TranslateService,
                public customersService: CustomersService,
                public router: Router,
                public route: ActivatedRoute,
                public location: Location,
                public customersViewService: CustomersViewService,
                public notifications: NotificationService) {
    }

    ngOnInit() {
        this.rowData = this.getRowData();
        this.pagination.totalElements = this.getNumberCustomers();
    }

    onPaginate(event) {
        this.customersService.getResources(event.page, event.rows)
            .subscribe(rows => {
                this.rowData = rows['_embedded'][REPOSITORY_NAME];
            });
    }

    onEditComplete(event) {
        let data: Customer = clone(event.data);

        this.customersService.updateResource(data)
            .subscribe(() => {
                this.notifications.createNotification('success', 'SUCCESS', 'customers.successUpdateCustomer');
            }, err => {
                console.error(err);
                this.notifications.createNotification('error', 'ERROR', 'customers.errorUpdateCustomer');

                return false;
            })
    }

    getRowData() {
        return this.route.snapshot.data['view'].rowData;
    }

    getNumberCustomers() {
        return this.route.snapshot.data['view'].totalElements;
    }
}
