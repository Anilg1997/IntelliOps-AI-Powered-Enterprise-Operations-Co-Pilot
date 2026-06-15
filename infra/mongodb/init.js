// IntelliOps - MongoDB Initialization Script
// Creates the inventory database with seed data for development/testing

db = db.getSiblingDB('intellops_inventory');

// ─── Categories ────────────────────────────────────────────────────────────

db.categories.insertMany([
    {
        name: 'Hardware',
        description: 'Physical IT equipment and accessories',
        parentCategory: null,
        active: true,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        name: 'Software',
        description: 'Software licenses and subscriptions',
        parentCategory: null,
        active: true,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        name: 'Security',
        description: 'Security appliances, certificates, and tools',
        parentCategory: 'Hardware',
        active: true,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        name: 'Services',
        description: 'Consulting and professional services',
        parentCategory: null,
        active: true,
        created_at: new Date(),
        updated_at: new Date()
    }
]);

// ─── Products ──────────────────────────────────────────────────────────────

db.products.createIndex({ sku: 1 }, { unique: true });

db.products.insertMany([
    {
        sku: 'SRV-RACK-42U',
        name: 'Enterprise Server Rack',
        description: '42U standard server rack with cooling system, cable management, and lockable doors',
        category: 'Hardware',
        price: 2499.99,
        specs: { form_factor: '42U', cooling: 'Dual Fan', material: 'Steel', weight: '85 kg' },
        tags: ['rack', 'server', 'infrastructure', 'datacenter'],
        active: true,
        image_url: null,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'CLD-STO-1TB',
        name: 'Cloud Storage License (1TB)',
        description: 'Annual subscription for 1TB cloud storage with redundancy and backup',
        category: 'Software',
        price: 599.99,
        specs: { capacity: '1TB', duration: 'Annual', redundancy: 'Geo-redundant' },
        tags: ['cloud', 'storage', 'subscription', 'backup'],
        active: true,
        image_url: null,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'NET-SW-48G',
        name: 'Network Switch 48-Port',
        description: 'Gigabit managed network switch, 48 ports, PoE+ support, VLAN capability',
        category: 'Hardware',
        price: 1299.99,
        specs: { ports: '48', speed: '1Gbps', poe: 'PoE+', managed: 'Yes' },
        tags: ['network', 'switch', 'gigabit', 'infrastructure'],
        active: true,
        image_url: null,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'SSL-WILD-1Y',
        name: 'SSL Certificate - Wildcard',
        description: '1-year wildcard SSL certificate for unlimited subdomains',
        category: 'Security',
        price: 349.99,
        specs: { type: 'Wildcard', duration: '1 Year', validation: 'Domain Validation', subdomains: 'Unlimited' },
        tags: ['ssl', 'security', 'certificate', 'wildcard'],
        active: true,
        image_url: null,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'DB-LIC-STD',
        name: 'Database License - Standard',
        description: 'Perpetual database license, standard edition, includes 5 user CALs',
        category: 'Software',
        price: 4999.99,
        specs: { edition: 'Standard', type: 'Perpetual', users: '5', support: '1 Year Included' },
        tags: ['database', 'license', 'software', 'enterprise'],
        active: true,
        image_url: null,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'FIB-LC10',
        name: 'Fiber Optic Cable (10m)',
        description: 'LC-LC duplex fiber optic patch cable, 10 meters, OS2 single-mode',
        category: 'Hardware',
        price: 29.99,
        specs: { length: '10m', connector: 'LC-LC', mode: 'Single-mode OS2', type: 'Duplex' },
        tags: ['fiber', 'cable', 'networking', 'patch'],
        active: true,
        image_url: null,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'FW-APPL-1U',
        name: 'Firewall Appliance',
        description: 'Next-generation firewall, 1U form factor, 1Gbps throughput, IPS/IDS',
        category: 'Security',
        price: 3899.99,
        specs: { form_factor: '1U', throughput: '1Gbps', features: 'IPS/IDS, VPN, DPI', users: '500' },
        tags: ['firewall', 'security', 'appliance', 'network'],
        active: true,
        image_url: null,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'CONS-10HR',
        name: 'Consulting Hours (10-pack)',
        description: 'Block of 10 hours enterprise architecture consulting with senior architect',
        category: 'Services',
        price: 4500.00,
        specs: { hours: '10', type: 'Enterprise Architecture', delivery: 'Remote or On-site' },
        tags: ['consulting', 'services', 'architecture', 'enterprise'],
        active: true,
        image_url: null,
        created_at: new Date(),
        updated_at: new Date()
    }
]);

// ─── Stock ──────────────────────────────────────────────────────────────────

db.stock.createIndex({ sku: 1 }, { unique: true });

db.stock.insertMany([
    {
        sku: 'SRV-RACK-42U',
        product_name: 'Enterprise Server Rack',
        total_quantity: 25,
        reserved_quantity: 3,
        warehouse_location: 'WH-NORTH-A12',
        restock_date: null,
        status: 'IN_STOCK',
        reorder_threshold: 5,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'CLD-STO-1TB',
        product_name: 'Cloud Storage License (1TB)',
        total_quantity: 100,
        reserved_quantity: 10,
        warehouse_location: 'WH-DIGITAL',
        restock_date: null,
        status: 'IN_STOCK',
        reorder_threshold: 20,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'NET-SW-48G',
        product_name: 'Network Switch 48-Port',
        total_quantity: 50,
        reserved_quantity: 5,
        warehouse_location: 'WH-NORTH-B07',
        restock_date: null,
        status: 'IN_STOCK',
        reorder_threshold: 10,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'SSL-WILD-1Y',
        product_name: 'SSL Certificate - Wildcard',
        total_quantity: 200,
        reserved_quantity: 15,
        warehouse_location: 'WH-DIGITAL',
        restock_date: null,
        status: 'IN_STOCK',
        reorder_threshold: 30,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'DB-LIC-STD',
        product_name: 'Database License - Standard',
        total_quantity: 30,
        reserved_quantity: 2,
        warehouse_location: 'WH-DIGITAL',
        restock_date: null,
        status: 'IN_STOCK',
        reorder_threshold: 5,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'FIB-LC10',
        product_name: 'Fiber Optic Cable (10m)',
        total_quantity: 500,
        reserved_quantity: 20,
        warehouse_location: 'WH-SOUTH-C03',
        restock_date: null,
        status: 'IN_STOCK',
        reorder_threshold: 100,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'FW-APPL-1U',
        product_name: 'Firewall Appliance',
        total_quantity: 15,
        reserved_quantity: 1,
        warehouse_location: 'WH-NORTH-A12',
        restock_date: null,
        status: 'IN_STOCK',
        reorder_threshold: 3,
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        sku: 'CONS-10HR',
        product_name: 'Consulting Hours (10-pack)',
        total_quantity: 999,
        reserved_quantity: 10,
        warehouse_location: 'WH-DIGITAL',
        restock_date: null,
        status: 'IN_STOCK',
        reorder_threshold: 50,
        created_at: new Date(),
        updated_at: new Date()
    }
]);
