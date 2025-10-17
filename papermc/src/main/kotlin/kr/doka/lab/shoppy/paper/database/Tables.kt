package kr.doka.lab.shoppy.paper.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * 상점 자체의 정보를 관리하는 테이블
 * 예: '기본상점', 'VIP상점' 등
 */
object Shops : IntIdTable("shops") {
    // 상점의 고유한 시스템 이름 (e.g., "default_shop", 커맨드 등에서 사용)
    val name = varchar("name", 50).uniqueIndex()

    // 상점 인벤토리의 크기 (9의 배수, 예: 27, 54)
    val size = integer("size").default(54)
}

/**
 * 각 상점에 어떤 아이템이 어디에 있는지 관리하는 테이블
 */
object ShopItems : IntIdTable("shop_items") {
    // 어떤 상점에 속한 아이템인지 (Shops 테이블의 ID를 참조)
    val shop = reference("shop_id", Shops, onDelete = ReferenceOption.CASCADE)

    // 상점 내 페이지 위치 ( page >= 0 )
    val page = integer("page")

    // 인벤토리 내 슬롯 위치 (0 ~ 53 등)
    val slot = integer("slot")

    // ItemStack을 Base64로 직렬화하여 저장할 컬럼
    val itemStackBase64 = text("itemstack_base64")

    // 아이템 판매 가격 (필수)
    val sellPrice = double("sell_price").default(0.0)

    // 아이템 구매 가격 (필수)
    val buyPrice = double("buy_price").default(0.0)
}
