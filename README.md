# AccountSystem

## 요구사항 분석
```
Account System은 사용자와 계좌의 정보를 저장하고 있으며,
외부 시스템에서 거래를 요청할 경우 거래 정보를 받아서 계좌에서 잔액을 거래 금액만큼 줄이거나(결제),
거래 금액만큼 늘리는(결제 취소) 거래 관리 기능을 제공하는 시스템이다.

크게 사용자, 계좌, 거래의 정보를 저장해야 한다.

사용자는 신규 등록, 해지, 중지, 사용자 정보 조회 등의 기능을 제공해야 하지만
최초 버전에서는 빠른 서비스 오픈을 위해 사용자 등록, 해지, 중지 기능은 제공하지 않고 DB로 수기 입력한다.

계좌는 계좌 추가, 해지, 확인 기능을 제공한다. 한 사용자는 최대 10개의 계좌를 가질 수 있으며 그 이상의 계좌는 생성하지 못한다.
계좌 번호는 10자리의 정수로 이루어지며 중복이 불가능하다.
빠르고 안정적인 진행을 위해 계좌번호는 순차 증가하도록 한다.

거래는 잔액 사용, 잔액 사용 취소, 거래 확인 기능을 제공한다.
```
