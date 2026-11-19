package com.example.ui.business.sheets

import androidx.compose.runtime.Composable
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessCategoryEntity
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.BusinessUiState
import com.example.ui.business.BusinessViewModel
import com.example.ui.business.tabs.isCapitalContributionTx
import com.example.ui.business.tabs.isCapitalMovementTx
import com.example.util.CurrencyFormatter

@Composable
internal fun BusinessSheetHost(
    navState: BusinessNavigationState,
    viewModel: BusinessViewModel,
    uiState: BusinessUiState,
    businessAccounts: List<BusinessAccountEntity>,
    businessCategories: List<BusinessCategoryEntity> = emptyList()
) {
    when (val sheet = navState.activeSheet) {
        BusinessActiveSheet.None -> Unit

        is BusinessActiveSheet.OrderForm -> {
            OrderFormSheet(
                uiState = uiState,
                editingOrder = sheet.editingOrder,
                initialArticle = sheet.initialArticle,
                onDismiss = { navState.closeSheet() },
                onCreateOrder = { product, supplier, qty, priceYuan, resaleAr, category, carrier, variants, freightAr, platform, refCode, family, domYuan, dateMs ->
                    viewModel.addBusinessOrder(
                        product = product,
                        supplier = supplier,
                        quantity = qty,
                        priceYuan = priceYuan,
                        resaleAr = resaleAr,
                        category = category,
                        carrier = carrier,
                        variants = variants,
                        freightAr = freightAr,
                        platform = platform,
                        referenceCode = refCode,
                        family = family,
                        domesticShippingYuan = domYuan,
                        orderDateMillis = dateMs
                    )
                },
                onUpdateOrder = { updated -> viewModel.updateBusinessOrder(updated) }
            )
        }

        is BusinessActiveSheet.AdjustStock -> {
            AdjustStockSheet(
                order = sheet.order,
                onDismiss = { navState.closeSheet() },
                onConfirm = { s, r, d, client ->
                    viewModel.adjustStock(sheet.order, s, r, d, client)
                }
            )
        }

        BusinessActiveSheet.AddArticle -> {
            ArticleFormSheet(
                uiState = uiState,
                editingArticle = null,
                onDismiss = { navState.closeSheet() },
                onAddArticle = { product, family, sku, variants, priceYuan, resaleAr, stockCount, weightKg, platform, imageUrl, freightMode, notes ->
                    viewModel.addCatalogueArticle(
                        product = product,
                        family = family,
                        referenceCode = sku,
                        variants = variants,
                        priceYuan = priceYuan,
                        resaleAr = resaleAr,
                        stockCount = stockCount,
                        weightKg = weightKg,
                        platform = platform,
                        imageUrl = imageUrl,
                        freightMode = freightMode,
                        notes = notes
                    )
                },
                onUpdateArticle = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> }
            )
        }

        is BusinessActiveSheet.EditArticle -> {
            ArticleFormSheet(
                uiState = uiState,
                editingArticle = sheet.article,
                onDismiss = { navState.closeSheet() },
                onAddArticle = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
                onUpdateArticle = { art, newProd, newSku, newFam, newPlat, newY, newResale, newW, newMode, newVar, newImg, newNotes, hidden ->
                    viewModel.updateCatalogueArticleFull(
                        article = art,
                        newProduct = newProd,
                        newReferenceCode = newSku,
                        newFamily = newFam,
                        newPlatform = newPlat,
                        newPriceYuan = newY,
                        newResaleAr = newResale,
                        newWeightKg = newW,
                        newFreightMode = newMode,
                        newVariants = newVar,
                        newImageUrl = newImg,
                        newNotes = newNotes,
                        isHidden = hidden
                    )
                }
            )
        }

        is BusinessActiveSheet.AddSale -> {
            AddSaleSheet(
                uiState = uiState,
                accounts = businessAccounts,
                initialArticle = sheet.initialArticle,
                onDismiss = { navState.closeSheet() },
                onCreateSale = { prod, client, phone, qty, unitP, unitC, paid, src, dom, linkedId, acc, dateMs ->
                    viewModel.addNewSale(prod, client, phone, qty, unitP, unitC, paid, src, dom, linkedId, acc, dateMs)
                }
            )
        }

        is BusinessActiveSheet.RecordSalePayment -> {
            val due = (sheet.sale.totalPriceAr - sheet.sale.paidAmountAr).coerceAtLeast(0L)
            RecordPaymentSheet(
                title = "Encaisser ${sheet.sale.clientName}",
                subtitle = "${sheet.sale.productName} · Reste ${CurrencyFormatter.formatAriary(due)}",
                maxDueAr = due,
                accounts = businessAccounts,
                onDismiss = { navState.closeSheet() },
                onConfirm = { amountAr, accName ->
                    viewModel.recordSalePayment(
                        sale = sheet.sale,
                        additionalPaymentAr = amountAr,
                        paymentMethod = accName.ifBlank { "ESPECES" },
                        accountName = accName
                    )
                }
            )
        }

        is BusinessActiveSheet.RecordClientPayment -> {
            RecordPaymentSheet(
                title = "Encaisser ${sheet.client.name}",
                subtitle = "Solde global : ${CurrencyFormatter.formatAriary(sheet.client.balanceDueAr)}",
                maxDueAr = sheet.client.balanceDueAr,
                accounts = businessAccounts,
                onDismiss = { navState.closeSheet() },
                onConfirm = { amountAr, accName ->
                    viewModel.recordClientPayment(sheet.client, amountAr, accName)
                }
            )
        }

        is BusinessActiveSheet.ClientDetail -> {
            val clientSales = uiState.sales.filter {
                (sheet.client.id in 1L..9999L && it.clientId == sheet.client.id) ||
                    it.clientName.trim().equals(sheet.client.name.trim(), ignoreCase = true)
            }.sortedByDescending { it.saleDate }
            ClientDetailSheet(
                client = sheet.client,
                clientSales = clientSales,
                onDismiss = { navState.closeSheet() },
                onTransitionSheet = { nextSheet -> navState.activeSheet = nextSheet }
            )
        }

        BusinessActiveSheet.AddClient -> {
            AddClientSheet(
                onDismiss = { navState.closeSheet() },
                onConfirm = { name, cat, phone, initialPurchase ->
                    viewModel.addClient(name, cat, phone, initialPurchase)
                }
            )
        }

        is BusinessActiveSheet.WhatsAppRelance -> {
            WhatsAppRelanceSheet(
                clientName = sheet.clientName,
                phone = sheet.phone,
                balanceDueAr = sheet.balanceDueAr,
                initialMessage = sheet.customMessage,
                onDismiss = { navState.closeSheet() }
            )
        }

        BusinessActiveSheet.BuyYuans, BusinessActiveSheet.PayPlatform -> {
            BuyYuansSheet(
                accounts = businessAccounts,
                defaultCmupRate = uiState.cmupRate,
                onDismiss = { navState.closeSheet() },
                onQuickCreateYuanAccount = { name ->
                    viewModel.addBusinessAccount(
                        name = name,
                        subtitle = "Chine (¥)",
                        initialBalanceAr = 0L,
                        isYuanAccount = true,
                        initialBalanceYuan = 0.0
                    )
                },
                onBuyYuans = { amountY, rateAr, platform, sourceAcc, dateMs ->
                    viewModel.buyYuans(amountY, rateAr, platform, sourceAcc, dateMs)
                }
            )
        }

        is BusinessActiveSheet.AddFinanceOperation -> {
            AddFinanceOperationSheet(
                accounts = businessAccounts,
                categories = businessCategories,
                presetTitle = sheet.presetTitle,
                presetType = sheet.presetType,
                presetTag = sheet.presetTag,
                onDismiss = { navState.closeSheet() },
                onManageCategories = { currentType ->
                    navState.activeSheet = BusinessActiveSheet.ManageCategories(currentType)
                },
                onConfirm = { title, type, amt, acc, tag, destAcc, categoryId ->
                    viewModel.addBusinessTransaction(title, type, amt, acc, tag, destAcc, categoryId)
                }
            )
        }

        is BusinessActiveSheet.AccountForm -> {
            AccountFormSheet(
                existing = sheet.existing,
                defaultIsYuan = sheet.defaultIsYuan,
                onDismiss = { navState.closeSheet() },
                onAdd = { name, sub, balAr, isY, balY ->
                    viewModel.addBusinessAccount(name, sub, balAr, isY, balY)
                },
                onUpdate = { existing, name, sub, balAr, balY ->
                    viewModel.updateBusinessAccount(existing, name, sub, balAr, balY)
                },
                onDelete = { acc -> viewModel.deleteBusinessAccount(acc) }
            )
        }

        is BusinessActiveSheet.PayFreightOrder -> {
            PayFreightOrderSheet(
                order = sheet.order,
                accounts = businessAccounts,
                onDismiss = { navState.closeSheet() },
                onConfirm = { order, amt, acc, dateMs -> viewModel.payOrderFreight(order, amt, acc, dateMs) }
            )
        }

        is BusinessActiveSheet.TransactionDetail -> {
            TransactionDetailSheet(
                tx = sheet.tx,
                categories = businessCategories,
                onDismiss = { navState.closeSheet() },
                onDelete = { tx -> viewModel.deleteBusinessTransaction(tx) },
                onEdit = if (isCapitalMovementTx(sheet.tx)) {
                    { tx ->
                        navState.activeSheet = BusinessActiveSheet.CapitalMovement(
                            isContribution = isCapitalContributionTx(tx),
                            existing = tx
                        )
                    }
                } else null
            )
        }

        is BusinessActiveSheet.YuanTransactionDetail -> {
            YuanTransactionDetailSheet(
                ytx = sheet.ytx,
                onDismiss = { navState.closeSheet() },
                onDelete = { ytx -> viewModel.deleteYuanTransaction(ytx) }
            )
        }

        is BusinessActiveSheet.CapitalMovement -> {
            CapitalMovementSheet(
                accounts = businessAccounts,
                initialIsContribution = sheet.isContribution,
                existing = sheet.existing,
                onDismiss = { navState.closeSheet() },
                onConfirm = { title, isContrib, amt, acc, note, affect, dateMs ->
                    viewModel.recordCapitalMovement(title, isContrib, amt, acc, note, affect, dateMs)
                },
                onUpdate = { existing, title, isContrib, amt, acc, note, affect, dateMs ->
                    viewModel.updateCapitalMovement(existing, title, isContrib, amt, acc, note, affect, dateMs)
                },
                onDelete = { tx -> viewModel.deleteBusinessTransaction(tx) }
            )
        }

        is BusinessActiveSheet.ManageCategories -> {
            ManageCategoriesSheet(
                categories = businessCategories,
                initialType = sheet.initialType,
                onDismiss = { navState.closeSheet() },
                onAdd = { name, type, iconKey, colorKey ->
                    viewModel.addBusinessCategory(name, type, iconKey, colorKey)
                },
                onUpdate = { existing, name, type, iconKey, colorKey ->
                    viewModel.updateBusinessCategory(existing, name, type, iconKey, colorKey)
                },
                onDelete = { cat ->
                    viewModel.deleteBusinessCategory(cat)
                }
            )
        }

        is BusinessActiveSheet.InvestmentForm -> {
            InvestmentFormSheet(
                existing = sheet.existing,
                accounts = businessAccounts,
                onDismiss = { navState.closeSheet() },
                onAdd = { title, cat, amt, est, ret, acc, notes, dateMs ->
                    viewModel.addBusinessInvestment(title, cat, amt, est, ret, acc, notes, dateMs)
                },
                onUpdate = { inv -> viewModel.updateBusinessInvestment(inv) }
            )
        }

        is BusinessActiveSheet.DebtForm -> {
            DebtFormSheet(
                existing = sheet.existing,
                accounts = businessAccounts,
                onDismiss = { navState.closeSheet() },
                onAdd = { title, dir, cp, principal, interest, dueMs, createdMs, freq, acc, affect, notes ->
                    viewModel.addBusinessDebt(title, dir, cp, principal, interest, dueMs, createdMs, freq, acc, affect, notes)
                },
                onUpdate = { debt -> viewModel.updateBusinessDebt(debt) }
            )
        }

        is BusinessActiveSheet.DebtPayment -> {
            DebtPaymentSheet(
                debt = sheet.debt,
                accounts = businessAccounts,
                onDismiss = { navState.closeSheet() },
                onConfirm = { debt, amt, acc, affect, note ->
                    viewModel.recordBusinessDebtPayment(debt, amt, acc, affect, System.currentTimeMillis(), note)
                }
            )
        }

        BusinessActiveSheet.AddFamily -> {
            AddFamilySheet(
                onDismiss = { navState.closeSheet() },
                onConfirm = { name, emoji -> viewModel.addCatalogueFamily(name, emoji) }
            )
        }

        BusinessActiveSheet.AddPlatform -> {
            AddPlatformSheet(
                onDismiss = { navState.closeSheet() },
                onConfirm = { name, sub, ship, sec -> viewModel.addCataloguePlatform(name, sub, ship, sec) }
            )
        }

        is BusinessActiveSheet.TransitaireForm -> {
            TransitaireFormSheet(
                existing = sheet.existing,
                onDismiss = { navState.closeSheet() },
                onAdd = { name, hub, air, sea, contact, addr, active, rateDate, airLead, seaLead, notes, airFast, airFastLead, goodsDelays ->
                    viewModel.addCatalogueTransitaire(name, hub, air, sea, contact, addr, active, rateDate, airLead, seaLead, notes, airFast, airFastLead, goodsDelays)
                },
                onUpdate = { id, name, hub, air, sea, contact, addr, active, rateDate, airLead, seaLead, notes, airFast, airFastLead, goodsDelays ->
                    viewModel.updateCatalogueTransitaire(id, name, hub, air, sea, contact, addr, active, rateDate, airLead, seaLead, notes, airFast, airFastLead, goodsDelays)
                }
            )
        }

        is BusinessActiveSheet.TransitaireTariffLine -> {
            val t = sheet.transitaire
            TransitaireTariffLineSheet(
                transitaire = t,
                existingItem = sheet.existingItem,
                onDismiss = { navState.closeSheet() },
                onSaveGrid = { sync ->
                    viewModel.updateCatalogueTransitaire(
                        id = t.id,
                        name = t.name,
                        hub = t.routeHub,
                        airRate = sync.airRateAr,
                        seaRate = sync.seaRateAr,
                        contact = t.contact,
                        chinaWarehouseAddress = t.chinaWarehouseAddress,
                        isActive = !t.isArchived,
                        rateApplicationDate = sync.rateApplicationDate,
                        airLeadDays = sync.airLeadDays,
                        seaLeadDays = sync.seaLeadDays,
                        internalNotes = t.internalNotes,
                        airFastRate = sync.airFastRateAr,
                        airFastLeadDays = sync.airFastLeadDays,
                        goodsCategoryDelays = sync.encodedGrid
                    )
                }
            )
        }

        is BusinessActiveSheet.PayTransitaire -> {
            PayTransitaireSheet(
                transitaire = sheet.transitaire,
                accounts = businessAccounts,
                onDismiss = { navState.closeSheet() },
                onConfirm = { amt, accName, dateMs ->
                    viewModel.recordTransitairePayment(sheet.transitaire.id, amt, accName, dateMs)
                }
            )
        }

        BusinessActiveSheet.Notifications -> {
            NotificationsSheet(
                notifications = uiState.businessNotifications,
                onDismiss = { navState.closeSheet() },
                onNotificationClick = { notif ->
                    navState.closeSheet()
                    if (notif.orderId != null) {
                        navState.openLot(notif.orderId)
                    } else if (!notif.clientName.isNullOrBlank()) {
                        navState.openClientRelanceByName(uiState.allClients, notif.clientName, 0L, "")
                    } else {
                        viewModel.onSelectSubTab(notif.targetSubTab)
                    }
                }
            )
        }
    }
}
